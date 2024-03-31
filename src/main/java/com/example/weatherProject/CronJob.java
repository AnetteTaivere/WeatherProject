package com.example.weatherProject;

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

    /**
     * Executes the task of scheduling and starting a Quartz job.
     * @throws SchedulerException If there is an error scheduling or starting the job.
     */
    public void executeTask() throws SchedulerException {
        // Define the job and tie it to class
        JobDetail job = JobBuilder.newJob(PullAndWriteData.class)
                .withIdentity("myJob", "group1")
                .build();

        // Define a Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronValue))
                .build();

        // Tell quartz to schedule the job
        (scheduler).scheduleJob(job, trigger);
        // Start the scheduler
        (scheduler).start();
    }
}