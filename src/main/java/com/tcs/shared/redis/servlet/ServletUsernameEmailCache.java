package com.tcs.shared.redis.servlet;

import java.time.Duration;
import java.util.Optional;

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
		String l1Email = l1Cache.getIfPresent(username);
		if (l1Email != null) {
			return Optional.of(l1Email);
		}

		try {
			String email = redisTemplate.opsForValue().get(redisKey(username));
			if (email != null) {
				l1Cache.put(username, email);
			}
			return Optional.ofNullable(email);
		}
		catch (RuntimeException ex) {
			logger.warn("Unable to read email cache for username {}", username, ex);
			return Optional.empty();
		}
	}

	public void put(String username, String email) {
		Assert.hasText(username, "username must not be blank");
		Assert.hasText(email, "email must not be blank");
		l1Cache.put(username, email);

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
