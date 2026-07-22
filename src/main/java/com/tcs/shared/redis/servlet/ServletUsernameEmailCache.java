package com.tcs.shared.redis.servlet;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Synchronous servlet-stack version of the username-to-email two-level cache.
 *
 * <p>Caffeine has no time-based expiry; it only evicts when the configured maximum size is
 * reached. Redis is the shared L2 cache and uses its separately configured TTL.</p>
 */
@Service
public class ServletUsernameEmailCache {

	private static final Logger logger = LoggerFactory.getLogger(ServletUsernameEmailCache.class);

	private final Cache<String, String> l1Cache;

	private final StringRedisTemplate redisTemplate;

	private final String redisKeyPrefix;

	private final Duration redisTtl;

	public ServletUsernameEmailCache(StringRedisTemplate redisTemplate,
			@Value("${app.cache.username-email.redis-key-prefix:username-email:}") String redisKeyPrefix,
			@Value("${app.cache.username-email.redis-ttl:12h}") Duration redisTtl,
			@Value("${app.cache.username-email.l1-maximum-size:50000}") long l1MaximumSize) {
		Assert.isTrue(l1MaximumSize > 0, "app.cache.username-email.l1-maximum-size must be positive");
		this.redisTemplate = redisTemplate;
		this.redisKeyPrefix = redisKeyPrefix;
		this.redisTtl = redisTtl;
		this.l1Cache = Caffeine.newBuilder().maximumSize(l1MaximumSize).build();
	}

	public Optional<String> get(String username) {
		Assert.hasText(username, "username must not be blank");
		return Optional.ofNullable(l1Cache.get(username, this::readFromRedis));
	}

	/**
	 * Gets an email from Caffeine or loads it atomically from Redis and the supplied source.
	 * Concurrent servlet requests for the same username wait for the same cache computation instead
	 * of issuing duplicate Redis or remote username lookups.
	 */
	public Optional<String> getOrLoad(String username, Function<String, String> emailLoader) {
		Assert.hasText(username, "username must not be blank");
		Assert.notNull(emailLoader, "emailLoader must not be null");
		return Optional.ofNullable(l1Cache.get(username, (key) -> {
			String cachedEmail = readFromRedis(key);
			if (cachedEmail != null) {
				return cachedEmail;
			}

			String loadedEmail = emailLoader.apply(key);
			if (loadedEmail == null || loadedEmail.isBlank()) {
				return null;
			}
			writeToRedis(key, loadedEmail);
			return loadedEmail;
		}));
	}

	public void put(String username, String email) {
		Assert.hasText(username, "username must not be blank");
		Assert.hasText(email, "email must not be blank");
		l1Cache.put(username, email);
		writeToRedis(username, email);
	}

	private String readFromRedis(String username) {
		try {
			return redisTemplate.opsForValue().get(redisKey(username));
		}
		catch (RuntimeException ex) {
			logger.warn("Unable to read email cache for username {}", username, ex);
			return null;
		}
	}

	private void writeToRedis(String username, String email) {
		try {
			redisTemplate.opsForValue().set(redisKey(username), email, redisTtl);
		}
		catch (RuntimeException ex) {
			logger.warn("Unable to write email cache for username {}", username, ex);
		}
	}

	private String redisKey(String username) {
		return redisKeyPrefix + username;
	}
}
