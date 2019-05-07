package com.sober.delay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author liweigao
 * @date 2018/12/8 下午4:16
 */
@Getter
@AllArgsConstructor
public enum PlanStatus {

    NORMAL(0),
    SUCCESS(1),
    FAILED(2),
    DELETE(9),
    ;
    private int code;
}
