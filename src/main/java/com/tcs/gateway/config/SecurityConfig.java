package com.tcs.gateway.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
			Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange((authorize) -> authorize
				.pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.anyExchange().authenticated())
			.oauth2ResourceServer((oauth2) -> oauth2
				.jwt((jwt) -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
			.build();
	}

	@Bean
	Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter scopeAuthorities = new JwtGrantedAuthoritiesConverter();

		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter((jwt) -> {
			List<GrantedAuthority> authorities = new ArrayList<>(scopeAuthorities.convert(jwt));
			authorities.addAll(cognitoGroupAuthorities(jwt));
			return authorities;
		});
		return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
	}

	private static Collection<GrantedAuthority> cognitoGroupAuthorities(Jwt jwt) {
		Object groups = jwt.getClaims().get("cognito:groups");
		if (groups instanceof Collection<?> groupCollection) {
			return groupCollection.stream()
				.map(String::valueOf)
				.filter((group) -> !group.isBlank())
				.map((group) -> new SimpleGrantedAuthority("ROLE_" + group))
				.map(GrantedAuthority.class::cast)
				.toList();
		}
		return List.of();
	}
}
