package com.tcs.gateway.security;

import reactor.core.publisher.Mono;

public interface UserEmailService {

	Mono<String> getEmail(String accessToken, String username);
}
