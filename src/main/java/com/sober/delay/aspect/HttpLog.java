package com.sober.delay.aspect;

import com.sober.delay.common.result.RestResult;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author liweigao
 * @date 2018/12/6 下午4:25
 */
@Data
@Builder
public class HttpLog {

    private Map header;

    private String method;

    private String reqUrl;

    private String reqUri;

    private Map reqParam;

    private String queryString;

    private String remoteHost;

    private RestResult responseBody;

    private Long reqTime;

    private Long costTime;
}
