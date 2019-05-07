package com.sober.delay.config;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * restTemplate log interceptor
 *
 * @author liweigao
 * @date 2018/12/25 下午2:06
 */
@Slf4j(topic = "outgoing")
public class LogClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ClientHttpResponse response = execution.execute(request, body);

        stopWatch.stop();
        StringBuilder resBody = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(),
                Charset.forName("UTF-8")))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                resBody.append(line);
                line = bufferedReader.readLine();
            }
        }

        if (request.getHeaders().getContentType() != null && request.getHeaders().getContentType().includes(MediaType.MULTIPART_FORM_DATA)) {
            body = new byte[]{};
        }

        log.info(JSON.toJSONString(RestLog.builder().costTime(stopWatch.getLastTaskTimeMillis()).headers(request.getHeaders()).method(request.getMethodValue())
                .reqUrl(request.getURI().toString()).reqBody(new String(body, Charset.forName("UTF-8"))).resBody(resBody.toString()).resStatus(response.getRawStatusCode()).build()));
        return response;
    }

    @Data
    @Builder
    @SuppressWarnings("rawtypes")
    private static class RestLog {
        private String reqUrl;
        private String method;
        private HttpHeaders headers;
        private String reqBody;
        private String resBody;
        private long costTime;
        private int resStatus;
    }
}
