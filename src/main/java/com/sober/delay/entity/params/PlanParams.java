package com.sober.delay.entity.params;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Maps;
import com.sober.delay.entity.PlanEntity;
import com.sober.delay.validate.group.Add;
import com.sober.delay.validate.group.Modify;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;


/**
 * @author liweigao
 * @date 2018/12/10 上午10:15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanParams {

    @NotBlank(groups = {Modify.class}, message = "id不能为空")
    private Integer id;
    @NotBlank(groups = {Add.class, Modify.class}, message = "计划名称不能为空")
    private String planName;
    @NotBlank(groups = {Add.class, Modify.class}, message = "回调地址不能为空")
    @Pattern(groups = {Add.class, Modify.class}, regexp = "^(?:https?://)?[\\w]{1,}(?:\\.?[\\w]{1,})+[\\w-_/?&=#%:]*$",
            message = "回调地址格式错误")
    private String callbackUrl;
    @NotBlank(groups = {Add.class, Modify.class}, message = "回调方法不能为空")
    private String callbackMethod;

    @Builder.Default
    private Map<String, Object> params = Maps.newHashMap();
    @Builder.Default
    private Map<String, String> headers = Maps.newHashMap();

    @Builder.Default
    private Integer planType = 0;
    private Integer retryNum;
    @NotNull(groups = {Add.class, Modify.class}, message = "执行时间不能为空")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date executeTime;


    public static PlanEntity conventEntity(PlanParams planParams) {
        PlanEntity planEntity = new PlanEntity();
        planEntity.setId(planParams.getId());
        planEntity.setCallbackUrl(planParams.getCallbackUrl());
        planEntity.setCallbackMethod(planParams.getCallbackMethod());
        planEntity.setPlanName(planParams.getPlanName());

        planEntity.setPlanType(planParams.getPlanType());
        if (!CollectionUtils.isEmpty(planParams.getParams())) {
            planEntity.setParams(JSON.toJSONString(planParams.getParams()));

        } else {
            planEntity.setParams("");
        }
        if (!CollectionUtils.isEmpty(planParams.getHeaders())) {
            planEntity.setHeaders(JSON.toJSONString(planParams.getHeaders()));
        } else {
            planEntity.setHeaders("");
        }
        planEntity.setRetryNum(planParams.getRetryNum() == null ? 0 : planParams.getRetryNum());
        planEntity.setExecuteTime(new Timestamp(planParams.getExecuteTime().getTime()));

        return planEntity;
    }
}
