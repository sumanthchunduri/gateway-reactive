package com.tcs.shared.redis.servlet;

import org.springframework.util.Assert;

public final class RedisCacheKeys {

	private RedisCacheKeys() {
	}

	public static String userData(String userId) {
		Assert.hasText(userId, "userId must not be blank");
		return "user-data:" + userId;
	}

	public static String userDetails(String userId) {
		Assert.hasText(userId, "userId must not be blank");
		return "user-details:" + userId;
	}

	public static String tenantDetails(String tenantId) {
		Assert.hasText(tenantId, "tenantId must not be blank");
		return "tenant-details:" + tenantId;
	}

	public static String accountDetails(String accountId) {
		Assert.hasText(accountId, "accountId must not be blank");
		return "account-details:" + accountId;
	}

	public static String entitlementDetails(String userId) {
		Assert.hasText(userId, "userId must not be blank");
		return "entitlement-details:" + userId;
	}

	public static String dataById(String namespace, String id) {
		Assert.hasText(namespace, "namespace must not be blank");
		Assert.hasText(id, "id must not be blank");
		return namespace + ":" + id;
	}
}
