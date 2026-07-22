package com.tcs.gateway.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.tcs.gateway.config.CognitoProperties;
import com.tcs.shared.cache.UsernameEmailCache;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CognitoEmailService implements UserEmailService {

	private final UsernameEmailCache emailCache;

	private final WebClient userInfoClient;

	private final CognitoProperties properties;

	public CognitoEmailService(UsernameEmailCache emailCache, WebClient.Builder webClientBuilder,
			CognitoProperties properties) {
		this.emailCache = emailCache;
		this.userInfoClient = webClientBuilder.build();
		this.properties = properties;
	}

	public Mono<String> getEmail(String accessToken, String username) {
		return emailCache.get(username)
			.switchIfEmpty(fetchEmail(accessToken)
				.flatMap((email) -> emailCache.put(username, email).thenReturn(email)));
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
