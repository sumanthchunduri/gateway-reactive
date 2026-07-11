package com.tcs.shared.redis.servlet.http;

import java.util.function.Supplier;

import com.tcs.shared.redis.servlet.RedisCacheKeys;
import com.tcs.shared.redis.servlet.RedisTemplateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DownstreamDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(DownstreamDetailsService.class);

	private final DeclarativeHttpClientFactory httpClientFactory;

	private final DownstreamClientProperties properties;

	private final UserServiceClient userServiceClient;

	private final TenantServiceClient tenantServiceClient;

	private final AccountServiceClient accountServiceClient;

	private final EntitlementServiceClient entitlementServiceClient;

	private final RedisTemplate<String, UserDetails> userDetailsRedisTemplate;

	private final RedisTemplate<String, TenantDetails> tenantDetailsRedisTemplate;

	private final RedisTemplate<String, AccountDetails> accountDetailsRedisTemplate;

	private final RedisTemplate<String, EntitlementDetails> entitlementDetailsRedisTemplate;

	public DownstreamDetailsService(DeclarativeHttpClientFactory httpClientFactory,
			DownstreamClientProperties properties,
			UserServiceClient userServiceClient,
			TenantServiceClient tenantServiceClient,
			AccountServiceClient accountServiceClient,
			EntitlementServiceClient entitlementServiceClient,
			RedisTemplateFactory redisTemplateFactory) {
		this.httpClientFactory = httpClientFactory;
		this.properties = properties;
		this.userServiceClient = userServiceClient;
		this.tenantServiceClient = tenantServiceClient;
		this.accountServiceClient = accountServiceClient;
		this.entitlementServiceClient = entitlementServiceClient;
		this.userDetailsRedisTemplate = redisTemplateFactory.create(UserDetails.class);
		this.tenantDetailsRedisTemplate = redisTemplateFactory.create(TenantDetails.class);
		this.accountDetailsRedisTemplate = redisTemplateFactory.create(AccountDetails.class);
		this.entitlementDetailsRedisTemplate = redisTemplateFactory.create(EntitlementDetails.class);
	}

	public UserDetails getUserDetails(String userId) {
		return getOrFetch(
				"user-service",
				RedisCacheKeys.userDetails(userId),
				userDetailsRedisTemplate,
				() -> userServiceClient.getUser(userId));
	}

	public TenantDetails getTenantDetails(String tenantId) {
		return getOrFetch(
				"tenant-service",
				RedisCacheKeys.tenantDetails(tenantId),
				tenantDetailsRedisTemplate,
				() -> tenantServiceClient.getTenant(tenantId));
	}

	public AccountDetails getAccountDetails(String accountId) {
		return getOrFetch(
				"account-service",
				RedisCacheKeys.accountDetails(accountId),
				accountDetailsRedisTemplate,
				() -> accountServiceClient.getAccount(accountId));
	}

	public EntitlementDetails getEntitlementDetails(String userId) {
		return getOrFetch(
				"entitlement-service",
				RedisCacheKeys.entitlementDetails(userId),
				entitlementDetailsRedisTemplate,
				() -> entitlementServiceClient.getEntitlements(userId));
	}

	private <T> T getOrFetch(String serviceName, String cacheKey, RedisTemplate<String, T> redisTemplate,
			Supplier<T> fetcher) {
		T cachedValue = readCache(cacheKey, redisTemplate);
		if (cachedValue != null) {
			return cachedValue;
		}

		T fetchedValue = httpClientFactory.invoke(serviceName, fetcher::get);
		if (fetchedValue != null) {
			writeCache(cacheKey, fetchedValue, redisTemplate);
		}
		return fetchedValue;
	}

	private <T> T readCache(String cacheKey, RedisTemplate<String, T> redisTemplate) {
		try {
			return redisTemplate.opsForValue().get(cacheKey);
		}
		catch (RuntimeException ex) {
			logger.warn("Unable to read Redis cache key {}", cacheKey, ex);
			return null;
		}
	}

	private <T> void writeCache(String cacheKey, T value, RedisTemplate<String, T> redisTemplate) {
		try {
			redisTemplate.opsForValue().set(cacheKey, value, properties.getCacheTtl());
		}
		catch (RuntimeException ex) {
			logger.warn("Unable to write Redis cache key {}", cacheKey, ex);
		}
	}
}
