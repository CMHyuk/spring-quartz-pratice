package com.example.quartz.quartz.scheduler.dto;

import com.example.quartz.quartz.job.dto.ScheduleJobSaveRequest;
import com.example.quartz.quartz.trigger.dto.JobTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.SimpleTriggerSaveRequest;

public record SimpleJobSaveRequest(
        ScheduleJobSaveRequest scheduleJobSaveRequest,
        JobTriggerSaveRequest jobTriggerSaveRequest,
        SimpleTriggerSaveRequest simpleTriggerSaveRequest
) {
}
