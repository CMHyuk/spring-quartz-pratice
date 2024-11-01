package com.example.quartz.quartz.trigger.util;

import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.TimeZone;

public class TriggerGenerator {

    public static Trigger createCronTrigger(JobCronTrigger jobCronTrigger) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobCronTrigger.getCronExpression())
                .inTimeZone(TimeZone.getTimeZone(jobCronTrigger.getTimeZone()));

        return TriggerBuilder.newTrigger()
                .withIdentity(jobCronTrigger.getTriggerName(), jobCronTrigger.getTriggerGroup())
                .withSchedule(jobCronTrigger.getMisFirePolicy().applyMisfirePolicy(cronScheduleBuilder))
                .build();
    }

}
