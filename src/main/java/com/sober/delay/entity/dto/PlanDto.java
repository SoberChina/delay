package com.sober.delay.entity.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Map;

/**
 * @author liweigao
 * @date 2018/12/8 下午2:38
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanDto {

    /**
     * 已执行次数
     */
    @Builder.Default
    private Integer retried = 0;
    private Integer id;
    private Integer planType;
    private String planName;
    private String planCode;
    private Integer state;
    private String callbackUrl;
    private String callbackMethod;
    private Map<String, Object> params;
    private Map<String, String> headers;
    private Integer retryNum;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date executeTime;
    private Date updateTime;
    private Date createTime;


}
