package com.example.quartz.quartz;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TriggerManager {

    private final Scheduler scheduler;

    public void updateTrigger(String triggerName, String cronExpression) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, "DEFAULT");
            Trigger oldTrigger = scheduler.getTrigger(triggerKey);
            if (oldTrigger == null) {
                throw new SchedulerException("트리거를 찾을 수 없습니다: " + triggerName);
            }

            Trigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(oldTrigger.getKey())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing())
                    .forJob(oldTrigger.getJobKey())
                    .build();

            scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
