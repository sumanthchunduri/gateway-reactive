package com.tcs.shared.cache;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

/**
 * Two-level cache for identity data used by gateway routes.
 *
 * <p>The in-process Caffeine cache intentionally has no time-based expiry. It is bounded by size
 * and evicts entries when it reaches capacity. Redis remains the shared L2 cache and retains its
 * configured TTL.</p>
 */
@Service
public class UsernameEmailCache {

	private static final Logger logger = LoggerFactory.getLogger(UsernameEmailCache.class);

	private final Cache<String, String> l1Cache;

	private final ReactiveStringRedisTemplate redisTemplate;

	private final String redisKeyPrefix;

	private final Duration redisTtl;

	public UsernameEmailCache(ReactiveStringRedisTemplate redisTemplate,
			@Value("${gateway.cognito.cache-key-prefix:}") String redisKeyPrefix,
			@Value("${gateway.cognito.email-cache-ttl:12h}") Duration redisTtl,
			@Value("${gateway.cognito.email-cache-l1-maximum-size:50000}") long l1MaximumSize) {
		Assert.isTrue(l1MaximumSize > 0, "gateway.cognito.email-cache-l1-maximum-size must be positive");
		this.redisTemplate = redisTemplate;
		this.redisKeyPrefix = redisKeyPrefix;
		this.redisTtl = redisTtl;
		this.l1Cache = Caffeine.newBuilder().maximumSize(l1MaximumSize).build();
	}

	public Mono<String> get(String username) {
		Assert.hasText(username, "username must not be blank");
		String l1Email = l1Cache.getIfPresent(username);
		if (l1Email != null) {
			return Mono.just(l1Email);
		}

		return redisTemplate.opsForValue().get(redisKey(username))
			.doOnNext((email) -> l1Cache.put(username, email))
			.onErrorResume((ex) -> {
				logger.warn("Unable to read email cache for username {}", username, ex);
				return Mono.empty();
			});
	}

	public Mono<Void> put(String username, String email) {
		Assert.hasText(username, "username must not be blank");
		Assert.hasText(email, "email must not be blank");
		l1Cache.put(username, email);

		return redisTemplate.opsForValue().set(redisKey(username), email, redisTtl)
			.onErrorResume((ex) -> {
				logger.warn("Unable to write email cache for username {}", username, ex);
				return Mono.just(false);
			})
			.then();
	}

	private String redisKey(String username) {
		return redisKeyPrefix + username;
	}
}
