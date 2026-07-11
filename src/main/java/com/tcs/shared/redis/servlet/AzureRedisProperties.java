package com.tcs.shared.redis.servlet;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis")
public class AzureRedisProperties {

	private String host;

	private int port = 6380;

	private boolean ssl = true;

	private String managedIdentityObjectId;

	private Duration commandTimeout = Duration.ofSeconds(5);

	private Duration tokenRefreshBeforeExpiry = Duration.ofMinutes(5);

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getManagedIdentityObjectId() {
		return managedIdentityObjectId;
	}

	public void setManagedIdentityObjectId(String managedIdentityObjectId) {
		this.managedIdentityObjectId = managedIdentityObjectId;
	}

	public Duration getCommandTimeout() {
		return commandTimeout;
	}

	public void setCommandTimeout(Duration commandTimeout) {
		this.commandTimeout = commandTimeout;
	}

	public Duration getTokenRefreshBeforeExpiry() {
		return tokenRefreshBeforeExpiry;
	}

	public void setTokenRefreshBeforeExpiry(Duration tokenRefreshBeforeExpiry) {
		this.tokenRefreshBeforeExpiry = tokenRefreshBeforeExpiry;
	}
}
