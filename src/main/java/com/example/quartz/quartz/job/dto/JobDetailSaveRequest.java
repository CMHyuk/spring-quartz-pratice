package com.example.quartz.quartz.job.dto;

import com.example.quartz.quartz.job.model.JobDetail;

public record JobDetailSaveRequest(String jobName, String jobGroup, String jobClassName, Boolean isDurable, Boolean requestRecovery) {

    public JobDetail toJobSchedule() {
        return JobDetail.of(
                this.jobName,
                this.jobGroup,
                this.jobClassName,
                this.isDurable,
                this.requestRecovery
        );
    }

}
