package com.tcs.gateway.config;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AzureRedisCredentialsProvider implements RedisCredentialsProvider {

	private static final String REDIS_SCOPE = "https://redis.azure.com/.default";

	private final TokenCredential tokenCredential;

	private final String username;

	private final Duration refreshBeforeExpiry;

	AzureRedisCredentialsProvider(TokenCredential tokenCredential, String username, Duration refreshBeforeExpiry) {
		this.tokenCredential = tokenCredential;
		this.username = username;
		this.refreshBeforeExpiry = refreshBeforeExpiry;
	}

	@Override
	public Mono<RedisCredentials> resolveCredentials() {
		return requestToken().map(this::toRedisCredentials);
	}

	@Override
	public boolean supportsStreaming() {
		return true;
	}

	@Override
	public Flux<RedisCredentials> credentials() {
		return Flux.defer(() -> requestToken()
			.expand((token) -> Mono.delay(refreshDelay(token)).then(requestToken()))
			.map(this::toRedisCredentials));
	}

	private Mono<AccessToken> requestToken() {
		return tokenCredential.getToken(new TokenRequestContext().addScopes(REDIS_SCOPE));
	}

	private RedisCredentials toRedisCredentials(AccessToken token) {
		return RedisCredentials.just(username, token.getToken().toCharArray());
	}

	private Duration refreshDelay(AccessToken token) {
		OffsetDateTime expiresAt = token.getExpiresAt();
		if (expiresAt == null) {
			return Duration.ofMinutes(30);
		}

		Duration delay = Duration.between(OffsetDateTime.now(), expiresAt.minus(refreshBeforeExpiry));
		if (delay.isNegative() || delay.isZero()) {
			return Duration.ofSeconds(30);
		}
		return delay;
	}
}
