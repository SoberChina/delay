package com.sober.delay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author liweigao
 * @date 2018/12/11 下午4:22
 */
@Getter
@AllArgsConstructor
public enum PlanFlag {

    /**
     *
     */
    NORMAL(0),

    COMPLETE(1),
    ;
    private Integer value;
}
