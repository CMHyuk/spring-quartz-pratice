package com.example.quartz.quartz.job.dto;

import com.example.quartz.quartz.job.model.ScheduleJob;

public record ScheduleJobSaveRequest(String jobName, String jobGroup, String jobClassName, Boolean isDurable, Boolean requestRecovery) {

    public ScheduleJob toJobSchedule() {
        return ScheduleJob.of(
                this.jobName,
                this.jobGroup,
                this.jobClassName,
                this.isDurable,
                this.requestRecovery
        );
    }

}
