package com.example.demo2;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


public class CronJob {

    public static void main(String[] args) throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        // Define the job and tie it to our MyJob class
        JobDetail job = JobBuilder.newJob(PullAndWriteData.class)
                .withIdentity("myJob", "group1")
                .build();

        // Define a Trigger that will fire every minute
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 * * ? * *")) // This will fire every minute
                .build();

        // Tell quartz to schedule the job using our trigger
        ((org.quartz.Scheduler) scheduler).scheduleJob(job, trigger);

        // Start the scheduler
        ((org.quartz.Scheduler) scheduler).start();
    }


}
