package com.tcs.shared.redis.servlet.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class DeclarativeHttpClientFactory {

	private final DownstreamClientProperties properties;

	public DeclarativeHttpClientFactory(DownstreamClientProperties properties) {
		this.properties = properties;
	}

	public <T> T create(String serviceName, String baseUrl, Class<T> clientType) {
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
			.connectTimeout(properties.getConnectTimeout())
			.build());
		requestFactory.setReadTimeout(properties.getReadTimeout());

		RestClient restClient = RestClient.builder()
			.baseUrl(baseUrl)
			.requestFactory(requestFactory)
			.defaultStatusHandler((status) -> status.is4xxClientError() || status.is5xxServerError(),
					(request, response) -> handleError(serviceName, response))
			.build();

		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
			.build()
			.createClient(clientType);
	}

	public <T> T invoke(String serviceName, HttpCall<T> call) {
		try {
			return call.execute();
		}
		catch (DownstreamServiceException ex) {
			throw ex;
		}
		catch (ResourceAccessException ex) {
			throw new DownstreamUnavailableException(serviceName, ex);
		}
		catch (RestClientException ex) {
			throw new DownstreamUnavailableException(serviceName, ex);
		}
	}

	private void handleError(String serviceName, ClientHttpResponse response) throws IOException {
		int statusCode = response.getStatusCode().value();
		String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

		if (statusCode == HttpStatus.NOT_FOUND.value()) {
			throw new DownstreamNotFoundException(serviceName, responseBody);
		}
		throw new DownstreamServiceException(serviceName, statusCode, responseBody);
	}

	@FunctionalInterface
	public interface HttpCall<T> {

		T execute();
	}
}
