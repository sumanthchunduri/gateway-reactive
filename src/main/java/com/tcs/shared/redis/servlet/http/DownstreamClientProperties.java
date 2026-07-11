package com.tcs.shared.redis.servlet.http;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.http.clients")
public class DownstreamClientProperties {

	private Duration connectTimeout = Duration.ofSeconds(3);

	private Duration readTimeout = Duration.ofSeconds(5);

	private Duration cacheTtl = Duration.ofMinutes(30);

	private String userServiceBaseUrl;

	private String tenantServiceBaseUrl;

	private String accountServiceBaseUrl;

	private String entitlementServiceBaseUrl;

	public Duration getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Duration getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Duration getCacheTtl() {
		return cacheTtl;
	}

	public void setCacheTtl(Duration cacheTtl) {
		this.cacheTtl = cacheTtl;
	}

	public String getUserServiceBaseUrl() {
		return userServiceBaseUrl;
	}

	public void setUserServiceBaseUrl(String userServiceBaseUrl) {
		this.userServiceBaseUrl = userServiceBaseUrl;
	}

	public String getTenantServiceBaseUrl() {
		return tenantServiceBaseUrl;
	}

	public void setTenantServiceBaseUrl(String tenantServiceBaseUrl) {
		this.tenantServiceBaseUrl = tenantServiceBaseUrl;
	}

	public String getAccountServiceBaseUrl() {
		return accountServiceBaseUrl;
	}

	public void setAccountServiceBaseUrl(String accountServiceBaseUrl) {
		this.accountServiceBaseUrl = accountServiceBaseUrl;
	}

	public String getEntitlementServiceBaseUrl() {
		return entitlementServiceBaseUrl;
	}

	public void setEntitlementServiceBaseUrl(String entitlementServiceBaseUrl) {
		this.entitlementServiceBaseUrl = entitlementServiceBaseUrl;
	}
}
