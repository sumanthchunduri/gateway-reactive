package com.tcs.shared.redis.servlet.http;

public class DownstreamServiceException extends RuntimeException {

	private final String serviceName;

	private final int statusCode;

	private final String responseBody;

	public DownstreamServiceException(String serviceName, int statusCode, String responseBody) {
		super("Downstream service " + serviceName + " returned HTTP " + statusCode);
		this.serviceName = serviceName;
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}

	public String getServiceName() {
		return serviceName;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getResponseBody() {
		return responseBody;
	}
}
