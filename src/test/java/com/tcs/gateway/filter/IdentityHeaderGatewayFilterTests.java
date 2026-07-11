package com.tcs.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.tcs.gateway.config.CognitoProperties;
import com.tcs.gateway.security.UserEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class IdentityHeaderGatewayFilterTests {

	@Test
	void replacesIdentityHeadersWithValuesFromAuthenticatedJwt() {
		UserEmailService emailService = new StubEmailService("suman@example.com");
		IdentityHeaderGatewayFilter filter = new IdentityHeaderGatewayFilter(emailService, new CognitoProperties());
		ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders")
			.header(IdentityHeaderGatewayFilter.USERNAME_HEADER, "spoofed")
			.header(IdentityHeaderGatewayFilter.EMAIL_HEADER, "spoofed@example.com"));
		exchange = exchange.mutate()
			.principal(Mono.just(authentication()))
			.build();

		AtomicReference<HttpHeaders> forwardedHeaders = new AtomicReference<>();
		GatewayFilterChain chain = (filteredExchange) -> {
			forwardedHeaders.set(filteredExchange.getRequest().getHeaders());
			return Mono.empty();
		};

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(forwardedHeaders.get().getFirst(IdentityHeaderGatewayFilter.USERNAME_HEADER)).isEqualTo("suman");
		assertThat(forwardedHeaders.get().getFirst(IdentityHeaderGatewayFilter.EMAIL_HEADER)).isEqualTo("suman@example.com");
		assertThat(forwardedHeaders.get().getFirst(IdentityHeaderGatewayFilter.ROLES_HEADER)).isEqualTo("admin");
	}

	private static JwtAuthenticationToken authentication() {
		Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(300),
				Map.of("alg", "none"),
				Map.of("username", "suman"));
		return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_admin")));
	}

	private static final class StubEmailService implements UserEmailService {

		private final String email;

		private StubEmailService(String email) {
			this.email = email;
		}

		@Override
		public Mono<String> getEmail(String accessToken, String username) {
			return Mono.just(email);
		}
	}
}
