package com.example.demo2;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CronJob {

    private final Scheduler scheduler;
    private final String cronValue;

    private CronJob(@Value("${cron.expression}") String cronValue) throws SchedulerException {
        this.scheduler = new StdSchedulerFactory().getScheduler();
        this.cronValue = cronValue;
    }


    public void executeTask() throws SchedulerException {

        // Define the job and tie it to our MyJob class
        JobDetail job = JobBuilder.newJob(PullAndWriteData.class)
                .withIdentity("myJob", "group1")
                .build();

        // Define a Trigger that will fire every minute
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronValue))
                .build();

        // Tell quartz to schedule the job using our trigger
        (scheduler).scheduleJob(job, trigger);

        // Start the scheduler
        (scheduler).start();
    }


}
