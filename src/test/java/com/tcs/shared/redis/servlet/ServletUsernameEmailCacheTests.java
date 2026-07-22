package com.tcs.shared.redis.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class ServletUsernameEmailCacheTests {

	@Test
	void promotesRedisValueToCaffeineForLaterServletRequests() {
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(values);
		when(values.get("username-email:alice")).thenReturn("alice@example.com");

		ServletUsernameEmailCache cache = new ServletUsernameEmailCache(
				redisTemplate, "username-email:", Duration.ofHours(12), 50_000);

		assertThat(cache.get("alice")).contains("alice@example.com");
		assertThat(cache.get("alice")).contains("alice@example.com");

		verify(values).get("username-email:alice");
	}

	@Test
	void getOrLoadCachesTheFetchedValueAfterOneRedisMiss() {
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(values);
		AtomicInteger loaderCalls = new AtomicInteger();
		ServletUsernameEmailCache cache = new ServletUsernameEmailCache(
				redisTemplate, "username-email:", Duration.ofHours(12), 50_000);

		assertThat(cache.getOrLoad("alice", (username) -> {
			loaderCalls.incrementAndGet();
			return "alice@example.com";
		})).contains("alice@example.com");
		assertThat(cache.getOrLoad("alice", (username) -> {
			loaderCalls.incrementAndGet();
			return "alice@example.com";
		})).contains("alice@example.com");

		assertThat(loaderCalls).hasValue(1);
		verify(values).get("username-email:alice");
		verify(values).set("username-email:alice", "alice@example.com", Duration.ofHours(12));
	}
}
