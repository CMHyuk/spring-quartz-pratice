package com.example.quartz.quartz.trigger.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

@Entity
@Getter
@Document(indexName = "job_trigger_" + "*", createIndex = false)
@Setting(settingPath = "lower_case_normalizer_setting.json")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobTrigger {

    @Id
    private String id;

    private String jobName;
    private String triggerName;
    private String triggerGroup;

    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    public static JobTrigger of(String jobName, String triggerName, String triggerGroup, TriggerType triggerType) {
        return new JobTrigger(jobName, triggerName, triggerGroup, triggerType);
    }

    private JobTrigger(String jobName, String triggerName, String triggerGroup, TriggerType triggerType) {
        this.jobName = jobName;
        this.triggerName = triggerName;
        this.triggerGroup = triggerGroup;
        this.triggerType = triggerType;
    }

}
