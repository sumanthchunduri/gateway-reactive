package com.tcs.gateway.filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tcs.gateway.config.CognitoProperties;
import com.tcs.gateway.security.UserEmailService;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class IdentityHeaderGatewayFilter implements GlobalFilter, Ordered {

	public static final String USERNAME_HEADER = "X-User-Name";

	public static final String EMAIL_HEADER = "X-User-Email";

	public static final String ROLES_HEADER = "X-User-Roles";

	private final UserEmailService emailService;

	private final CognitoProperties properties;

	public IdentityHeaderGatewayFilter(UserEmailService emailService, CognitoProperties properties) {
		this.emailService = emailService;
		this.properties = properties;
	}

	@Override
	public @NonNull Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return exchange.getPrincipal()
			.cast(Authentication.class)
			.filter(Authentication::isAuthenticated)
			.ofType(JwtAuthenticationToken.class)
			.flatMap((authentication) -> filterWithIdentityHeaders(exchange, chain, authentication))
			.switchIfEmpty(chain.filter(exchange));
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 10;
	}

	private Mono<Void> filterWithIdentityHeaders(ServerWebExchange exchange, GatewayFilterChain chain,
			JwtAuthenticationToken authentication) {
		Jwt jwt = authentication.getToken();
		String username = username(jwt);
		if (username.isBlank()) {
			return chain.filter(exchange);
		}

		return emailService.getEmail(jwt.getTokenValue(), username)
			.defaultIfEmpty("")
			.flatMap((email) -> {
				ServerHttpRequest request = exchange.getRequest().mutate()
					.headers((headers) -> {
						headers.remove(USERNAME_HEADER);
						headers.remove(EMAIL_HEADER);
						headers.remove(ROLES_HEADER);
						headers.set(USERNAME_HEADER, username);
						if (!email.isBlank()) {
							headers.set(EMAIL_HEADER, email);
						}
						String roles = roles(authentication);
						if (!roles.isBlank()) {
							headers.set(ROLES_HEADER, roles);
						}
					})
					.build();
				return chain.filter(exchange.mutate().request(request).build());
			});
	}

	private String username(Jwt jwt) {
		String configuredClaim = jwt.getClaimAsString(properties.getUsernameClaim());
		if (configuredClaim != null && !configuredClaim.isBlank()) {
			return configuredClaim;
		}

		return Stream.of("username", "cognito:username", "sub")
			.map(jwt::getClaimAsString)
			.filter(Objects::nonNull)
			.filter((claim) -> !claim.isBlank())
			.findFirst()
			.orElse("");
	}

	private static String roles(Authentication authentication) {
		return authentication.getAuthorities().stream()
			.map((authority) -> Objects.requireNonNull(authority.getAuthority()).replaceFirst("^ROLE_", ""))
			.filter((role) -> !role.startsWith("SCOPE_"))
			.collect(Collectors.joining(","));
	}
}
