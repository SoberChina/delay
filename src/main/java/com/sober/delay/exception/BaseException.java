package com.sober.delay.exception;

import com.sober.delay.enums.Error;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liweigao
 * @date 2018/12/6 下午4:55
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseException extends RuntimeException {
    private int code;
    private String message;

    public BaseException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseException(Error error) {
        this.code = error.getCode();
        this.message = error.getMsg();
    }

}
