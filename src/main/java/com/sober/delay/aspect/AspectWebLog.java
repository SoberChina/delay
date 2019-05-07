package com.sober.delay.aspect;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.sober.delay.common.result.RestResult;
import com.sober.delay.enums.Error;
import com.sober.delay.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author liweigao
 * @date 2018/11/15 下午3:32
 */
@Slf4j(topic = "http")
@Aspect
@Component
public class AspectWebLog {

    @Pointcut("execution(public org.springframework.http.ResponseEntity<com." +
            "sober.delay.common.result.RestResult> " +
            "com.sober.delay.web.*Controller.*(..))")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint pjp) {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        Object o;
        RestResult result = null;
        try {
            o = pjp.proceed();
        } catch (Throwable throwable) {
            o = handlerException(throwable);
            result = (RestResult) ((ResponseEntity) o).getBody();
        }

        if (o != null) {
            if (o instanceof ResponseEntity && ((ResponseEntity) o).getBody() instanceof RestResult) {
                result = (RestResult) ((ResponseEntity) o).getBody();
            }
        }

        long costTime = System.currentTimeMillis() - startTime;
        Map params = null;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            try {
                params = objectArrayToMap(pjp.getArgs());
            } catch (Exception e) {
                log.error("Get POST param error", e);
            }
            if (null == params || params.size() < 1) {
                params = getParams(request.getParameterMap());
            }
        } else {
            params = getParams(request.getParameterMap());
        }

        log(request, params, result, startTime, costTime);
        return o;
    }

    private Map<String, String> getParams(Map<String, String[]> mapParams) {
        if (mapParams == null || mapParams.size() == 0) {
            return null;
        }

        Map map = Maps.newHashMap();
        mapParams.forEach((k, v) -> {
            map.put(k, v[0]);
        });
        return map;
    }

    public static void log(HttpServletRequest request, Map params, RestResult result, long startTime,
                           long costTime) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteHost();
        }
        HttpLog httpLog = HttpLog.builder()
                .header(getBusinessHeader(request))
                .reqUrl(request.getRequestURL().toString())
                .reqUri(request.getRequestURI())
                .method(request.getMethod())
                .queryString(request.getQueryString())
                .reqParam(params)
                .remoteHost(ipAddress)
                .responseBody(result)
                .reqTime(startTime)
                .costTime(costTime)
                .build();
        try {
            log.info(JSON.toJSONString(httpLog));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static Map getBusinessHeader(HttpServletRequest request) {
        Map map = Maps.newHashMap();
        Enumeration e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String headerName = (String) e.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            if (null != headerValues) {
                map.put(headerName, headerValues.nextElement());
            }
        }

        return map;
    }

    //捕获切面类的异常信息
    private ResponseEntity<RestResult> handlerException(Throwable e) {

        if (e instanceof BaseException) {
            return ResponseEntity.ok(RestResult.builder().code(((BaseException) e).getCode())
                    .msg(e.getMessage()).build());
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            List<ObjectError> allErrors = ex.getAllErrors();
            ObjectError error = allErrors.get(0);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResult.builder()
                    .code(Error.BAD_REQUEST.getCode()).msg(error.getDefaultMessage()).build());

        } else if (e instanceof ConstraintViolationException) {
            StringBuffer errorMsg = new StringBuffer();
            ((ConstraintViolationException) e).getConstraintViolations().forEach(violation -> {
                errorMsg.append(violation.getMessage());
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResult.builder()
                    .code(Error.BAD_REQUEST.getCode()).msg(errorMsg.toString()).build());

        } else {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestResult.fail());
        }
    }


    /**
     * object 转map
     */
    private static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (Objects.isNull(obj)) {
            return null;
        }

        Map<String, Object> map = Maps.newHashMap();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0 || key.compareToIgnoreCase("empty") == 0
                    || key.compareToIgnoreCase("bytes") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = getter != null ? getter.invoke(obj) : null;
            map.put(key, value);
        }

        return map;
    }


    /**
     * object 转map
     */
    public static Map<String, Object> objectArrayToMap(Object[] obj) throws Exception {
        if (Objects.isNull(obj)) {
            return null;
        }
        Map<String, Object> map = Maps.newHashMap();

        for (Object o : obj) {
            Map<String, Object> m = objectToMap(o);
            if (m != null && m.size() > 0) {
                map.putAll(m);
            }

        }
        return map;
    }
}