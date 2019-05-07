package com.sober.delay.config;

import com.google.common.collect.Lists;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * @author liweigao
 * @date 2018/12/8 下午2:56
 */
@Configuration
@EnableConfigurationProperties(RestTemplateProperties.class)
public class RestTemplateConfig {

    private final RestTemplateProperties restTemplateProperties;

    public RestTemplateConfig(RestTemplateProperties restTemplateProperties) {
        this.restTemplateProperties = restTemplateProperties;
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory httpRequestFactory) {
        return getRestTemplate(httpRequestFactory);
    }

    @LoadBalanced
    @Bean(name = "innerRestTemplate")
    public RestTemplate innerRestTemplate(ClientHttpRequestFactory httpRequestFactory) {
        return getRestTemplate(httpRequestFactory);
    }

    private RestTemplate getRestTemplate(ClientHttpRequestFactory httpRequestFactory) {
        RestTemplate restTemplate = new RestTemplate();
        /**
         * StringHttpMessageConverter 默认使用ISO-8859-1编码，此处修改为UTF-8
         */
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
        while (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(Charset.forName("UTF-8"));
            }
        }

        //Interceptors
        restTemplate.setInterceptors(Lists.newArrayList(new RequestIdInterceptor(),
                new LogClientHttpRequestInterceptor()));
        //BufferingClientHttpRequestFactory
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(httpRequestFactory));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory(HttpClient httpClient) {
        /**
         * Spring使用；两种方式实现http请求
         * 1.SimpleClientHttpRequestFactory，使用J2SE提供的方式（既java.net包提供的方式）创建底层的Http请求连接
         * 2.HttpComponentsClientHttpRequestFactory，底层使用HttpClient访问远程的Http服务，使用HttpClient可以配置连接池和证书等信息
         */

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    public HttpClient httpClient() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(restTemplateProperties.getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(restTemplateProperties.getDefaultMaxPerRoute());

        RequestConfig requestConfig = RequestConfig.custom()
                //读取超时时间
                .setSocketTimeout(restTemplateProperties.getSocketTimeout())
                //连接超时时间
                .setConnectTimeout(restTemplateProperties.getConnectTimeout())
                .setConnectionRequestTimeout(restTemplateProperties.getConnectionRequestTimeout())
                .build();

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                //设置重试次数，默认三次未开启
                .setRetryHandler(new DefaultHttpRequestRetryHandler(restTemplateProperties.getRetryCount(),
                        restTemplateProperties.isRequestSentRetryEnabled()))
                .build();
    }
}