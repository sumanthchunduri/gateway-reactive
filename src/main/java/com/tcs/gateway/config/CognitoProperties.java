package com.tcs.gateway.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.cognito")
public class CognitoProperties {

	private String userInfoUri;

	private String usernameClaim = "username";

	private String emailClaim = "email";

	private String cacheKeyPrefix = "";

	private Duration emailCacheTtl = Duration.ofHours(12);

	public String getUserInfoUri() {
		return userInfoUri;
	}

	public void setUserInfoUri(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}

	public String getUsernameClaim() {
		return usernameClaim;
	}

	public void setUsernameClaim(String usernameClaim) {
		this.usernameClaim = usernameClaim;
	}

	public String getEmailClaim() {
		return emailClaim;
	}

	public void setEmailClaim(String emailClaim) {
		this.emailClaim = emailClaim;
	}

	public String getCacheKeyPrefix() {
		return cacheKeyPrefix;
	}

	public void setCacheKeyPrefix(String cacheKeyPrefix) {
		this.cacheKeyPrefix = cacheKeyPrefix;
	}

	public Duration getEmailCacheTtl() {
		return emailCacheTtl;
	}

	public void setEmailCacheTtl(Duration emailCacheTtl) {
		this.emailCacheTtl = emailCacheTtl;
	}
}
