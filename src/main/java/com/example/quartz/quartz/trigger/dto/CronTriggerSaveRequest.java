package com.example.quartz.quartz.trigger.dto;

import com.example.quartz.quartz.trigger.model.JobCronTrigger;

public record CronTriggerSaveRequest(String triggerName, String triggerGroup, String cronExpression) {

    public JobCronTrigger toCronTrigger() {
        return JobCronTrigger.of(this.triggerName, this.triggerGroup, this.cronExpression);
    }

}
