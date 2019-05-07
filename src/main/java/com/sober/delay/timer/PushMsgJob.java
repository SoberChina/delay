package com.sober.delay.timer;

import com.sober.delay.common.Constant;
import com.sober.delay.common.RedisLock;
import com.sober.delay.config.RedisEntityConfig;
import com.sober.delay.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * @author liweigao
 * @date 2018/12/10 下午7:45
 */
@Slf4j
@DisallowConcurrentExecution
public class PushMsgJob extends QuartzJobBean {

    @Autowired
    private PlanService planService;

    @Autowired
    private RedisEntityConfig redisEntityConfig;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {

        log.info(jobExecutionContext.toString());

        RedisLock redisLock = new RedisLock(redisEntityConfig.getRedisTemplate(),
                Constant.RedisConstant.getPushDataLockKey());

        try {
            if (redisLock.acquire()) {
                Long timeStamp = System.currentTimeMillis();
                planService.autoPush(new Date(timeStamp + Constant.ADVANCE_FLAG),
                        new Date(timeStamp + Constant.TASK_CYCLE + Constant.ADVANCE_FLAG));
                //释放锁
                redisLock.release();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
