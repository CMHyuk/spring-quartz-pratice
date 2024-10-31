package com.example.quartz.quartz.scheduler.dto;

import com.example.quartz.quartz.job.dto.ScheduleJobSaveRequest;
import com.example.quartz.quartz.trigger.dto.CronTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.JobTriggerSaveRequest;

public record CronJobSaveRequest(
        ScheduleJobSaveRequest scheduleJobSaveRequest,
        JobTriggerSaveRequest jobTriggerSaveRequest,
        CronTriggerSaveRequest cronTriggerSaveRequest
) {
}
