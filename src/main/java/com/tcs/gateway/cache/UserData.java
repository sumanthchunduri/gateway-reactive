package com.tcs.gateway.cache;

import java.time.Instant;
import java.util.Map;

public record UserData(
		String userId,
		String email,
		String displayName,
		Map<String, Object> attributes,
		Instant updatedAt
) {
}
