package com.tcs.gateway.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.tcs.gateway.cache.RedisJsonTemplateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.RedisCredentialsProviderFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.RedisCredentialsProvider;

@Configuration
public class AzureRedisConfig {

	@Bean
	TokenCredential azureTokenCredential() {
		return new DefaultAzureCredentialBuilder().build();
	}

	@Bean
	LettuceConnectionFactory redisConnectionFactory(AzureRedisProperties properties,
			TokenCredential tokenCredential) {
		RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(
				properties.getHost(), properties.getPort());
		redisConfiguration.setUsername(properties.getManagedIdentityObjectId());

		LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder =
				LettuceClientConfiguration.builder()
					.commandTimeout(properties.getCommandTimeout())
					.redisCredentialsProviderFactory(new RedisCredentialsProviderFactory() {
						@Override
						public RedisCredentialsProvider createCredentialsProvider(RedisConfiguration redisConfig) {
							return new AzureRedisCredentialsProvider(
									tokenCredential,
									properties.getManagedIdentityObjectId(),
									properties.getTokenRefreshBeforeExpiry());
						}
					});

		LettuceClientConfiguration clientConfiguration = properties.isSsl()
				? clientBuilder.useSsl().and().build()
				: clientBuilder.build();

		return new LettuceConnectionFactory(redisConfiguration, clientConfiguration);
	}

	@Bean
	ReactiveStringRedisTemplate reactiveStringRedisTemplate(LettuceConnectionFactory connectionFactory) {
		return new ReactiveStringRedisTemplate(connectionFactory);
	}

	@Bean
	ReactiveRedisTemplate<String, Object> reactiveJsonRedisTemplate(LettuceConnectionFactory connectionFactory) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer.builder()
			.enableUnsafeDefaultTyping()
			.build();

		RedisSerializationContext<String, Object> serializationContext =
				RedisSerializationContext.<String, Object>newSerializationContext(keySerializer)
					.value(valueSerializer)
					.hashKey(keySerializer)
					.hashValue(valueSerializer)
					.build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}

	@Bean
	RedisJsonTemplateFactory redisJsonTemplateFactory(LettuceConnectionFactory connectionFactory) {
		return new RedisJsonTemplateFactory(connectionFactory);
	}
}
