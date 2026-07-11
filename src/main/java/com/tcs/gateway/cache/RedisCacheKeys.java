package com.tcs.gateway.cache;

import org.springframework.util.Assert;

public final class RedisCacheKeys {

	private RedisCacheKeys() {
	}

	public static String userData(String userId) {
		Assert.hasText(userId, "userId must not be blank");
		return "user-data:" + userId;
	}

	public static String dataById(String namespace, String id) {
		Assert.hasText(namespace, "namespace must not be blank");
		Assert.hasText(id, "id must not be blank");
		return namespace + ":" + id;
	}
}
