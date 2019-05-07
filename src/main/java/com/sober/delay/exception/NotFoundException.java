package com.sober.delay.exception;

import com.sober.delay.enums.Error;

/**
 * @author liweigao
 * @date 2018/12/8 下午3:15
 */
public class NotFoundException extends BizException {

    public NotFoundException(String msg) {
        super(Error.NOT_FOUND.getCode(), msg);
    }

    public NotFoundException() {
        super(Error.NOT_FOUND);
    }

    public NotFoundException(Error error) {
        super(error);
    }
}
