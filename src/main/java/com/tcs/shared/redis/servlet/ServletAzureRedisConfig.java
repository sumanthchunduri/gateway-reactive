package com.tcs.shared.redis.servlet;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.RedisCredentialsProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.RedisCredentialsProviderFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(AzureRedisProperties.class)
public class ServletAzureRedisConfig {

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
	StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	@Bean
	RedisTemplate<String, Object> objectJsonRedisTemplate(RedisConnectionFactory connectionFactory) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		GenericJacksonJsonRedisSerializer valueSerializer = GenericJacksonJsonRedisSerializer.builder()
			.enableUnsafeDefaultTyping()
			.build();

		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(keySerializer);
		template.setValueSerializer(valueSerializer);
		template.setHashKeySerializer(keySerializer);
		template.setHashValueSerializer(valueSerializer);
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	RedisTemplateFactory redisTemplateFactory(RedisConnectionFactory connectionFactory) {
		return new RedisTemplateFactory(connectionFactory);
	}
}
