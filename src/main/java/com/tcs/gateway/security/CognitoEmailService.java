package com.tcs.gateway.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.tcs.gateway.config.CognitoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CognitoEmailService implements UserEmailService {

	private static final Logger logger = LoggerFactory.getLogger(CognitoEmailService.class);

	private final ReactiveStringRedisTemplate redisTemplate;

	private final WebClient userInfoClient;

	private final CognitoProperties properties;

	public CognitoEmailService(ReactiveStringRedisTemplate redisTemplate, WebClient.Builder webClientBuilder,
			CognitoProperties properties) {
		this.redisTemplate = redisTemplate;
		this.userInfoClient = webClientBuilder.build();
		this.properties = properties;
	}

	public Mono<String> getEmail(String accessToken, String username) {
		String cacheKey = properties.getCacheKeyPrefix() + username;

		return redisTemplate.opsForValue().get(cacheKey)
			.onErrorResume((ex) -> {
				logger.warn("Unable to read Cognito email cache for username {}", username, ex);
				return Mono.empty();
			})
			.switchIfEmpty(fetchEmail(accessToken)
				.flatMap((email) -> redisTemplate.opsForValue()
					.set(cacheKey, email, properties.getEmailCacheTtl())
					.onErrorResume((ex) -> {
						logger.warn("Unable to write Cognito email cache for username {}", username, ex);
						return Mono.just(false);
					})
					.thenReturn(email)));
	}

	private Mono<String> fetchEmail(String accessToken) {
		return userInfoClient.get()
			.uri(properties.getUserInfoUri())
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.retrieve()
			.bodyToMono(JsonNode.class)
			.map(this::emailFromUserInfo)
			.filter((email) -> !email.isBlank());
	}

	private String emailFromUserInfo(JsonNode userInfo) {
		JsonNode emailNode = userInfo.path(properties.getEmailClaim());
		if (!emailNode.isTextual()) {
			return "";
		}
		return emailNode.asText();
	}
}
