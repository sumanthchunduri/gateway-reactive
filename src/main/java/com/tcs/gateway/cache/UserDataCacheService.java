package com.tcs.gateway.cache;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserDataCacheService {

	private final ReactiveRedisTemplate<String, UserData> redisTemplate;

	private final Duration ttl;

	public UserDataCacheService(RedisJsonTemplateFactory templateFactory,
			@Value("${gateway.cache.user-data-ttl:30m}") Duration ttl) {
		this.redisTemplate = templateFactory.create(UserData.class);
		this.ttl = ttl;
	}

	public Mono<UserData> get(String userId) {
		return redisTemplate.opsForValue().get(RedisCacheKeys.userData(userId));
	}

	public Mono<Boolean> put(UserData userData) {
		return redisTemplate.opsForValue().set(RedisCacheKeys.userData(userData.userId()), userData, ttl);
	}

	public Mono<Boolean> delete(String userId) {
		return redisTemplate.delete(RedisCacheKeys.userData(userId)).map((deleted) -> deleted > 0);
	}
}
