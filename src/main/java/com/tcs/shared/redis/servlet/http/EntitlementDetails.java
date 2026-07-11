package com.tcs.shared.redis.servlet.http;

import java.util.List;
import java.util.Map;

public record EntitlementDetails(
		String userId,
		List<String> permissions,
		Map<String, Object> attributes
) {
}
