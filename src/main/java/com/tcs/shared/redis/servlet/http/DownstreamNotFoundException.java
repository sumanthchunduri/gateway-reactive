package com.tcs.shared.redis.servlet.http;

public class DownstreamNotFoundException extends DownstreamServiceException {

	public DownstreamNotFoundException(String serviceName, String responseBody) {
		super(serviceName, 404, responseBody);
	}
}
