package com.example.quartz.quartz.trigger.dto;

import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.MisfirePolicy;

public record CronTriggerSaveRequest(
        String triggerName,
        String triggerGroup,
        String timeZone,
        String misFirePolicy,
        CronExpressionUpdateRequest cronExpressionRequest
) {

    public JobCronTrigger toCronTrigger(String cronExpression) {
        return JobCronTrigger.of(
                this.triggerName,
                this.triggerGroup,
                this.timeZone,
                MisfirePolicy.from(this.misFirePolicy),
                cronExpression
        );
    }

}
