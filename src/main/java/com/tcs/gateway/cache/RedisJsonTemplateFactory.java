package com.tcs.gateway.cache;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisJsonTemplateFactory {

	private final ReactiveRedisConnectionFactory connectionFactory;

	public RedisJsonTemplateFactory(ReactiveRedisConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public <T> ReactiveRedisTemplate<String, T> create(Class<T> valueType) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		JacksonJsonRedisSerializer<T> valueSerializer = new JacksonJsonRedisSerializer<>(valueType);

		RedisSerializationContext<String, T> serializationContext =
				RedisSerializationContext.<String, T>newSerializationContext(keySerializer)
					.value(valueSerializer)
					.hashKey(keySerializer)
					.hashValue(valueSerializer)
					.build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}
}
