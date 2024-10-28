package com.example.quartz.quartz.trigger.dto;

import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.model.TriggerType;

public record JobTriggerSaveRequest(String jobName, String triggerName, String triggerGroup, String triggerType) {

    public JobTrigger toTriggerSchedule() {
        return JobTrigger.of(
                this.jobName,
                this.triggerName,
                this.triggerGroup,
                TriggerType.valueOf(this.triggerType.toUpperCase())
        );
    }

}
