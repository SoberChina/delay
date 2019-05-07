package com.sober.delay.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * requestId interceptor
 *
 * @author liweigao
 * @date 2018/12/25 下午2:52
 */
public class RequestIdInterceptor implements ClientHttpRequestInterceptor {

    private static final String REQUEST_ID = "X-Request-Id";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (CollectionUtils.isEmpty(request.getHeaders().get(REQUEST_ID))) {
            request.getHeaders().set(REQUEST_ID, UUID.randomUUID().toString());
        }
        return execution.execute(request, body);
    }

}
