package com.sober.delay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author liweigao
 * @date 2018/12/7 下午2:23
 */
@Getter
@AllArgsConstructor
public enum Error {
    /**
     *
     */

    INNER_ERROR(10000500, "服务器内部异常"),

    BAD_REQUEST(10000400, "参数异常"),
    NOT_FOUND(10000404, "资源未找到"),
    PLAN_EXECUTE_ERROR(10001001, "任务执行失败"),

    PLAN_DELAY_TIME_EXCEEDING_THE_LIMIT(10001002, "任务设定时间过长,目前只支持3小时内延迟"),

    PLAN_IS_NOT_FOUND(10001003, "计划code数据未找到"),
    FUNCTION_NOT_IMPLEMENTED(10001004, "功能未实现"),
    COMPLETED_PLAN_NOT_SUPPORT_DELETE(10001005, "已执行完的计划不能删除"),

    MIME_TYPE_NOT_MATCHING(10001006, "资源类型不匹配"),

    REST_TEMPLATE_IO_EXCEPTION(10001007, "rest 请求错误"),
    ;


    private int code;
    private String msg;
}
