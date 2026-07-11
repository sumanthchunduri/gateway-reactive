package com.tcs.shared.redis.servlet.http;

import java.util.Map;

public record UserDetails(
		String userId,
		String email,
		String displayName,
		Map<String, Object> attributes
) {
}
