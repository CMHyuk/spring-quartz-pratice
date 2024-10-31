package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobSimpleTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobCronTriggerRepository;
import com.example.quartz.quartz.trigger.repository.JobSimpleTriggerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TriggerFactory {

    private final JobCronTriggerRepository jobCronTriggerRepository;
    private final JobSimpleTriggerRepository jobSimpleTriggerRepository;

    public Trigger createTrigger(JobTrigger jobTrigger) {
        return switch (jobTrigger.getTriggerType()) {
            case CRON -> createCronTrigger(jobTrigger);
            case SIMPLE -> createSimpleTrigger(jobTrigger);
        };
    }

    private Trigger createCronTrigger(JobTrigger jobTrigger) {
        String triggerName = jobTrigger.getTriggerName();
        String triggerGroup = jobTrigger.getTriggerGroup();

        JobCronTrigger jobCronTrigger = jobCronTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .orElseThrow(EntityNotFoundException::new);

        return TriggerBuilder.newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(jobCronTrigger.getCronExpression())
                        //.inTimeZone(TimeZone.getTimeZone(jobCronTrigger.getTimeZone()))
                        .withMisfireHandlingInstructionFireAndProceed())
                .forJob(jobTrigger.getJobName())
                .build();
    }

    private Trigger createSimpleTrigger(JobTrigger jobTrigger) {
        String triggerName = jobTrigger.getTriggerName();
        String triggerGroup = jobTrigger.getTriggerGroup();

        JobSimpleTrigger jobSimpleTrigger = jobSimpleTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .orElseThrow(EntityNotFoundException::new);

        return TriggerBuilder.newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(jobSimpleTrigger.getRepeatInterval())
                        .withRepeatCount(jobSimpleTrigger.getRepeatCount())
                        .withMisfireHandlingInstructionFireNow())
                .forJob(jobTrigger.getJobName())
                .build();
    }

}