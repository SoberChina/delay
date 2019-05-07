package com.sober.delay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liweigao
 * @date 2018/12/17 下午12:11
 */
@Getter
@Setter
@ConfigurationProperties("rest-template.common")
public class RestTemplateProperties {
    /**
     * 通信超时
     */
    private int socketTimeout = 10000;

    /**
     * 连接超时
     */
    private int connectTimeout = 10000;
    /**
     * 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，等待时间过长将是灾难性的
     */
    private int connectionRequestTimeout = 200;
    /**
     * 最大并发
     */
    private int maxTotal = 1000;
    /**
     * 同路由的并发数
     */
    private int defaultMaxPerRoute = Double.valueOf(maxTotal * 0.8).intValue();

    /**
     * 重试
     */
    private int retryCount = 2;

    /**
     * 是否重试
     */
    private boolean requestSentRetryEnabled = true;
}
