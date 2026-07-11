package com.tcs.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"COGNITO_ISSUER_URI=https://example.com/issuer",
		"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/.well-known/jwks.json",
		"gateway.cognito.user-info-uri=https://example.com/oauth2/userInfo",
		"gateway.redis.host=localhost",
		"gateway.redis.managed-identity-object-id=test-managed-identity-object-id"
})
class GatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
