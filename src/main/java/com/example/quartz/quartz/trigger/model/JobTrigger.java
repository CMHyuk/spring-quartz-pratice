package com.example.quartz.quartz.trigger.model;

import com.example.quartz.global.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import static lombok.AccessLevel.*;

@Entity
@Getter
@Document(indexName = "job_trigger_" + "*", createIndex = false)
@Setting(settingPath = "lower_case_normalizer_setting.json")
@NoArgsConstructor(access = PROTECTED)
public class JobTrigger extends BaseEntity {

    private String jobName;
    private String jobGroup;
    private String triggerName;
    private String triggerGroup;

    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    public static JobTrigger of(String jobName, String jobGroup, String triggerName, String triggerGroup, TriggerType triggerType) {
        return new JobTrigger(jobName, jobGroup, triggerName, triggerGroup, triggerType);
    }

    private JobTrigger(String jobName, String jobGroup, String triggerName, String triggerGroup, TriggerType triggerType) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.triggerName = triggerName;
        this.triggerGroup = triggerGroup;
        this.triggerType = triggerType;
    }

}
