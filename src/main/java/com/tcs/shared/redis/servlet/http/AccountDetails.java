package com.tcs.shared.redis.servlet.http;

import java.util.Map;

public record AccountDetails(
		String accountId,
		String accountType,
		String status,
		Map<String, Object> attributes
) {
}
