package com.tcs.shared.redis.servlet;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDataRedisRepository {

	private final RedisTemplate<String, UserData> redisTemplate;

	private final Duration ttl;

	public UserDataRedisRepository(RedisTemplateFactory templateFactory,
			@Value("${app.redis.user-data-ttl:30m}") Duration ttl) {
		this.redisTemplate = templateFactory.create(UserData.class);
		this.ttl = ttl;
	}

	public Optional<UserData> get(String userId) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(RedisCacheKeys.userData(userId)));
	}

	public void put(UserData userData) {
		redisTemplate.opsForValue().set(RedisCacheKeys.userData(userData.userId()), userData, ttl);
	}

	public boolean delete(String userId) {
		return Boolean.TRUE.equals(redisTemplate.delete(RedisCacheKeys.userData(userId)));
	}
}
