package com.example.quartz.quartz.scheduler.dto;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;

public record JobSaveMessage(ScheduleJob scheduleJob, JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
}
