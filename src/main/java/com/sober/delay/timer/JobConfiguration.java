package com.sober.delay.timer;

import org.quartz.*;
import org.springframework.stereotype.Component;


/**
 * @author liweigao
 * @date 2018/12/11 下午4:09
 */
@Component
public class JobConfiguration {

    public JobConfiguration(Scheduler scheduler) throws Exception {

        pushDataConfig(scheduler);
        deleteDataConfig(scheduler);

    }

    private void pushDataConfig(Scheduler scheduler) throws Exception {
        //任务名称
        String name = "delay_pushDate_timer";
        //任务所属分组
        String group = "every_30_minutes";
        //每30分钟点执行一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/30 * * * ?");
        //创建任务
        JobDetail jobDetail = JobBuilder.newJob(PushMsgJob.class).withIdentity(name, group).build();
        //创建任务触发器
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group).withSchedule(scheduleBuilder).build();
        //将触发器与任务绑定到调度器内

        scheduler.scheduleJob(jobDetail, trigger);
    }

    private void deleteDataConfig(Scheduler scheduler) throws Exception {
        //任务名称
        String name = "delay_deleteData_timer";
        //任务所属分组
        String group = "every_day";
        //每天23点10分执行一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 10 23 1/1 * ?");
        //        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/2 * * * ?");
        //创建任务
        JobDetail jobDetail = JobBuilder.newJob(DeleteDataJob.class).withIdentity(name, group).build();
        //创建任务触发器
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group).withSchedule(scheduleBuilder).build();
        //将触发器与任务绑定到调度器内

        scheduler.scheduleJob(jobDetail, trigger);
    }
}
