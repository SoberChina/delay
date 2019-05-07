package com.sober.delay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：plan触发执行类型
 *
 * @author liweigao
 * @date 2018/12/8 下午3:23
 */
@Getter
@AllArgsConstructor
public enum ExecuteType {

    /**
     * 自动执行
     */
    AUTO(0),
    /**
     * 手动执行
     */
    MANUAL(1),
    ;
    private int code;
}
