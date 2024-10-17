package com.example.quartz.schedulejob;

public record ScheduleJobRequest(
        String jobName,
        String cronExpression,
        String jobClass,
        String triggerName
) {

    public ScheduleJob toScheduleJob() {
        return ScheduleJob.of(
                this.jobName,
                this.cronExpression,
                this.jobClass,
                this.triggerName
        );
    }

}
