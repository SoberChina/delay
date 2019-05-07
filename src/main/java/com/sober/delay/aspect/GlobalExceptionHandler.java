package com.sober.delay.aspect;


import com.sober.delay.common.result.RestResult;
import com.sober.delay.enums.Error;
import com.sober.delay.exception.BaseException;
import com.sober.delay.exception.BizException;
import com.sober.delay.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * GlobalExceptionHandler : 全局异常处理, 通用的异常在此进行处理
 * 排除aop切面日志类异常。
 */
@Slf4j
@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理资源未找到异常
     *
     * @param ex 异常栈
     * @return responseEntity
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<RestResult> handleException(NotFoundException ex, HttpServletRequest req) {
        log.warn("NotFundException: code :{}, msg:{},exception:{}", ex.getCode(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestResult.builder().code(ex.getCode()).msg(ex.getMessage()).build());
    }


    /**
     * 处理业务异常
     *
     * @param ex 异常栈
     * @return responseEntity
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<RestResult> handleException(BizException ex) {
        log.warn("BizException: code :{}, msg:{},exception:{}", ex.getCode(), ex.getMessage(), ex);

        return ResponseEntity.ok(RestResult.builder().code(ex.getCode()).msg(ex.getMessage()).build());
    }

    /**
     * 处理业务未分类异常
     *
     * @param ex 异常栈
     * @return responseEntity  http status:200
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<RestResult> handleException(BaseException ex) {
        log.warn("TicketException: code :{}, msg:{},exception:{}", ex.getCode(), ex.getMessage(), ex);

        return ResponseEntity.ok(RestResult.builder().code(ex.getCode()).msg(ex.getMessage()).build());
    }


    /**
     * 处理mvc参数类型异常
     *
     * @param ex 异常栈
     * @return responseEntity http status:200
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestResult> handleException(MethodArgumentTypeMismatchException ex,
                                                      HttpServletRequest request) {
        log.error(String.format("Exception: name : %s, msg : %s", ex.getName(), ex.getMessage()), ex);

        RestResult result = RestResult.failWithMsg("param:" + ex.getName() + "   " + ex.getMessage());
        AspectWebLog.log(request, null, result, 0L, 0L);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestResult.error(Error.INNER_ERROR));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RestResult> handleException(MissingServletRequestParameterException ex) {
        log.error("request err msg:{}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResult.error(Error.BAD_REQUEST));
    }

    /**
     * 处理未分类异常
     *
     * @param ex 异常栈
     * @return responseEntity http status:200
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResult> handleException(Exception ex) {
        log.error(ex.getMessage(), ex.getMessage());
        if (ex instanceof MethodArgumentNotValidException) {
            List<ObjectError> allErrors = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
            //捕获的所有错误对象
            ObjectError error = allErrors.get(0);
            String defaultMessage = error.getDefaultMessage();
            //异常内容
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResult.builder().code(Error.BAD_REQUEST.getCode()).msg(defaultMessage).build());
        }
        if (ex instanceof ServletRequestBindingException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResult.error(Error.BAD_REQUEST));
        }
        if (ex instanceof NoHandlerFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestResult.error(Error.NOT_FOUND));
        }


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestResult.error(Error.INNER_ERROR));
    }
}
