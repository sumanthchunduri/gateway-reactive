package com.tcs.shared.redis.servlet.http;

import java.util.Map;

public record TenantDetails(
		String tenantId,
		String name,
		Map<String, Object> settings
) {
}
