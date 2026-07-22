package com.tcs.shared.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

class UsernameEmailCacheTests {

	@Test
	void readsFromCaffeineAfterWritingToBothCacheLevels() {
		ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ReactiveValueOperations<String, String> values = mock(ReactiveValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(values);
		when(values.set("email:alice", "alice@example.com", Duration.ofHours(12))).thenReturn(Mono.just(true));

		UsernameEmailCache cache = new UsernameEmailCache(redisTemplate, "email:", Duration.ofHours(12), 50_000);

		cache.put("alice", "alice@example.com").block();

		assertThat(cache.get("alice").block()).isEqualTo("alice@example.com");
		verify(values).set("email:alice", "alice@example.com", Duration.ofHours(12));
		verify(values, never()).get("email:alice");
	}
}
