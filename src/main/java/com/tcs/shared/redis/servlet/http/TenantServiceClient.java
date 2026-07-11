package com.tcs.shared.redis.servlet.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface TenantServiceClient {

	@GetExchange("/tenants/{tenantId}")
	TenantDetails getTenant(@PathVariable String tenantId);
}
