package com.tcs.shared.redis.servlet.http;

public class DownstreamUnavailableException extends RuntimeException {

	private final String serviceName;

	public DownstreamUnavailableException(String serviceName, Throwable cause) {
		super("Downstream service " + serviceName + " is unavailable", cause);
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}
}
