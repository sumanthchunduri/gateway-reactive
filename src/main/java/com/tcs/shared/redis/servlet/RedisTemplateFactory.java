package com.tcs.shared.redis.servlet;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisTemplateFactory {

	private final RedisConnectionFactory connectionFactory;

	public RedisTemplateFactory(RedisConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public <T> RedisTemplate<String, T> create(Class<T> valueType) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		JacksonJsonRedisSerializer<T> valueSerializer = new JacksonJsonRedisSerializer<>(valueType);

		RedisTemplate<String, T> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(keySerializer);
		template.setValueSerializer(valueSerializer);
		template.setHashKeySerializer(keySerializer);
		template.setHashValueSerializer(valueSerializer);
		template.afterPropertiesSet();
		return template;
	}
}
