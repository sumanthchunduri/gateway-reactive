package com.tcs.shared.redis.servlet.http;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DownstreamClientProperties.class)
public class DeclarativeHttpClientConfig {

	@Bean
	DeclarativeHttpClientFactory declarativeHttpClientFactory(DownstreamClientProperties properties) {
		return new DeclarativeHttpClientFactory(properties);
	}

	@Bean
	UserServiceClient userServiceClient(DeclarativeHttpClientFactory factory,
			DownstreamClientProperties properties) {
		return factory.create("user-service", properties.getUserServiceBaseUrl(), UserServiceClient.class);
	}

	@Bean
	TenantServiceClient tenantServiceClient(DeclarativeHttpClientFactory factory,
			DownstreamClientProperties properties) {
		return factory.create("tenant-service", properties.getTenantServiceBaseUrl(), TenantServiceClient.class);
	}

	@Bean
	AccountServiceClient accountServiceClient(DeclarativeHttpClientFactory factory,
			DownstreamClientProperties properties) {
		return factory.create("account-service", properties.getAccountServiceBaseUrl(), AccountServiceClient.class);
	}

	@Bean
	EntitlementServiceClient entitlementServiceClient(DeclarativeHttpClientFactory factory,
			DownstreamClientProperties properties) {
		return factory.create("entitlement-service", properties.getEntitlementServiceBaseUrl(),
				EntitlementServiceClient.class);
	}
}
