package com.sober.delay.timer;

import com.sober.delay.config.AutoDeleteProperties;
import com.sober.delay.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.Objects;

/**
 * @author liweigao
 * @date 2019/4/4 下午4:05
 */
@Slf4j
@DisallowConcurrentExecution
public class DeleteDataJob extends QuartzJobBean {

    private final PlanService planService;

    private final AutoDeleteProperties autoDeleteProperties;

    public DeleteDataJob(PlanService planService, AutoDeleteProperties autoDeleteProperties) {

        this.autoDeleteProperties = autoDeleteProperties;
        this.planService = planService;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        log.info(jobExecutionContext.toString());

        if (!autoDeleteProperties.isEnabled()) {

            log.info("auto delete config is off");
            return;
        }
        if (Objects.isNull(autoDeleteProperties.getRetainDays()) || autoDeleteProperties.getRetainDays() < 0) {

            log.info("auto delete config retainDays is error retainDays:{}", autoDeleteProperties.getRetainDays());
            return;
        }
        try {
            planService.autoDeleteData(new Date(System.currentTimeMillis()
                    - autoDeleteProperties.getRetainDays() * 24 * 60 * 60 * 1000L));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }
}
