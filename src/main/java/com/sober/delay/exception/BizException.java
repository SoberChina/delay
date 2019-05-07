package com.sober.delay.exception;

import com.sober.delay.enums.Error;

/**
 * @author liweigao
 * @date 2018/12/7 下午2:22
 */
public class BizException extends BaseException {

    public BizException(Integer code, String msg) {
        super(code, msg);
    }

    public BizException(Error error) {
        super(error);
    }
}
