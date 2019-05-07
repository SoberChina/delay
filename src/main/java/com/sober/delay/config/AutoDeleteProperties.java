package com.sober.delay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liweigao
 * @date 2019/4/8 上午11:15
 */
@Data
@Component
@ConfigurationProperties(prefix = "auto.delete")
public class AutoDeleteProperties {

    /**
     * 开关 true/false  default:false
     */
    private boolean enabled = false;

    /**
     * 保留天数 Integer unsigned
     */
    private Integer retainDays = 5;

}