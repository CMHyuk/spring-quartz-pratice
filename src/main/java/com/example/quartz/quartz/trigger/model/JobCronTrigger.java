package com.example.quartz.quartz.trigger.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

@Entity
@Getter
@Document(indexName = "cron_trigger_" + "*", createIndex = false)
@Setting(settingPath = "lower_case_normalizer_setting.json")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobCronTrigger {

    @Id
    private String id;

    private String triggerName;
    private String triggerGroup;
    private String cronExpression;

    public static JobCronTrigger of(String triggerName, String triggerGroup, String cronExpression) {
        return new JobCronTrigger(triggerName, triggerGroup, cronExpression);
    }

    private JobCronTrigger(String triggerName, String triggerGroup, String cronExpression) {
        this.triggerName = triggerName;
        this.triggerGroup = triggerGroup;
        this.cronExpression = cronExpression;
    }

    public void updateCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

}
