package com.example.quartz.quartz.trigger.model;

import com.example.quartz.global.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import static lombok.AccessLevel.*;

@Entity
@Getter
@Document(indexName = "cron_trigger_" + "*", createIndex = false)
@Setting(settingPath = "lower_case_normalizer_setting.json")
@NoArgsConstructor(access = PROTECTED)
public class JobCronTrigger extends BaseEntity {

    private String triggerName;
    private String triggerGroup;
    private String timeZone;
    private MisfirePolicy misFirePolicy;
    private String cronExpression;

    public static JobCronTrigger of(String triggerName, String triggerGroup, String timeZone, MisfirePolicy misFirePolicy, String cronExpression) {
        return new JobCronTrigger(triggerName, triggerGroup, timeZone, misFirePolicy, cronExpression);
    }

    private JobCronTrigger(String triggerName, String triggerGroup, String timeZone, MisfirePolicy misFirePolicy, String cronExpression) {
        this.triggerName = triggerName;
        this.triggerGroup = triggerGroup;
        this.timeZone = timeZone;
        this.misFirePolicy = misFirePolicy;
        this.cronExpression = cronExpression;
    }

    public void updateCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

}
