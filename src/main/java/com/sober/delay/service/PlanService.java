package com.sober.delay.service;

import com.sober.delay.common.result.Page;
import com.sober.delay.entity.dto.PlanDto;
import com.sober.delay.entity.params.PlanParams;
import com.sober.delay.enums.ExecuteType;

import java.util.Date;
import java.util.List;

/**
 * @author liweigao
 * @date 2018/12/8 下午2:37
 */
public interface PlanService {

    PlanDto save(PlanParams planParams);

    void deleteByCode(String planCode);

    PlanDto modifyByCode(PlanParams planParams);

    List<PlanDto> list();

    /**
     * 分页查询
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return
     */
    Page<PlanDto> list(Integer page, Integer pageSize);

    PlanDto findByCode(String planCode);

    void executePlan(String planCode);

    void executePlan(PlanDto planCode, ExecuteType executeType);

    /**
     * push 消息
     *
     * @param planDto
     * @param expireTime
     */
    void pushData(PlanDto planDto, Long expireTime);

    /**
     * 定时任务推送数据
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     */
    void autoPush(Date beginTime, Date endTime) throws InterruptedException;

    /**
     * 删除数据
     */
    void autoDeleteData(Date executeTime);


}
