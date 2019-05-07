package com.sober.delay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author liweigao
 * @date 2019/1/22 下午7:05
 */
@Getter
@AllArgsConstructor
public enum PlanType {

    /**
     * 内部eureka调用
     */
    EUREKA(0),

    /**
     * 普通ip调用
     */
    IP(1);
    private Integer type;

}
