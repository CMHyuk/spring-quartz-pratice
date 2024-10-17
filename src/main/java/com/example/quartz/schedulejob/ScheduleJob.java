package com.example.quartz.schedulejob;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

@Entity
@Getter
@Document(indexName = "schedule_job_" + "*", createIndex = false)
@Setting(settingPath = "lower_case_normalizer_setting.json")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleJob {

    @Id
    private String id;

    private String jobName;
    private String cronExpression;
    private String jobClass;
    private String triggerName;

    public static ScheduleJob of(String jobName, String cronExpression, String jobClass, String triggerName) {
        return new ScheduleJob(jobName, cronExpression, jobClass, triggerName);
    }

    private ScheduleJob(String jobName, String cronExpression, String jobClass, String triggerName) {
        this.jobName = jobName;
        this.cronExpression = cronExpression;
        this.jobClass = jobClass;
        this.triggerName = triggerName;
    }

    public void updateCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

}
