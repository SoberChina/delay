package com.sober.delay.config;

import com.sober.delay.enums.LoadStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liweigao
 * @date 2019/5/8 上午11:03
 */
@Data
@Component
@ConfigurationProperties(prefix = "delay.config")
public class DelayProperties {
    /**
     * 默认 latest
     */
    private LoadStrategy loadStrategy = LoadStrategy.LATEST;
}
