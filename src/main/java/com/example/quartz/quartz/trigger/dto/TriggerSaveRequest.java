package com.example.quartz.quartz.trigger.dto;

public record TriggerSaveRequest(JobTriggerSaveRequest jobTriggerSaveRequest, CronTriggerSaveRequest cronTriggerSaveRequest) {
}
