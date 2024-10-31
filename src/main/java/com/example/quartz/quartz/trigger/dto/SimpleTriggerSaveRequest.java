package com.example.quartz.quartz.trigger.dto;

import com.example.quartz.quartz.trigger.model.JobSimpleTrigger;

public record SimpleTriggerSaveRequest(String triggerName, String triggerGroup, Long repeatInterval, Integer repeatCount) {

    public JobSimpleTrigger toSimpleTrigger() {
        return JobSimpleTrigger.of(
                this.triggerName,
                this.triggerGroup,
                this.repeatInterval,
                this.repeatCount
        );
    }

}
