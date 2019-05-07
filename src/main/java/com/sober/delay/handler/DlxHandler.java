package com.sober.delay.handler;

import com.alibaba.fastjson.JSON;
import com.sober.delay.common.Constant;
import com.sober.delay.config.RabbitMqBeanConfig;
import com.sober.delay.dao.PlanDao;
import com.sober.delay.entity.dto.PlanDto;
import com.sober.delay.enums.ExecuteType;
import com.sober.delay.enums.PlanStatus;
import com.sober.delay.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liweigao
 * @date 2018/12/8 下午2:50
 */
@Slf4j
@Component
public class DlxHandler {

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanDao planDao;

    /**
     * {@link org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer#setConcurrency(String)}
     *
     * @param msg msg
     */
    @RabbitListener(queues = RabbitMqBeanConfig.DELAY_CONSUMER_DLX_QUEUE, concurrency = "5-10")
    public void onMsg(String msg) {
        log.info(msg);
        PlanDto planDto;
        boolean flag = false;
        try {
            planDto = JSON.parseObject(msg, PlanDto.class);
        } catch (Exception e) {
            log.error("数据解析失败，消息丢弃！");
            return;
        }
        try {
            planService.executePlan(planDto, ExecuteType.AUTO);
            flag = true;
        } catch (Exception e) {
            log.error(e.getMessage());
            if (planDto.getRetried() < planDto.getRetryNum()) {
                log.info("retry");
                //重试测试+1
                planDto.setRetried(planDto.getRetried() + 1);
                //延迟重试 延迟一分钟 允许误差100msL
                planService.pushData(planDto, planDto.getRetried() * 60000L + Constant.FAULT_TOLERANT_REQUEST_TIME);
            }
        } finally {
            //更新状态
            planDao.modifyStateById(flag ? PlanStatus.SUCCESS.getCode() : PlanStatus.FAILED.getCode(), planDto.getId());
        }
    }
}
