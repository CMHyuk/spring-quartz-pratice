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
public class JobSimpleTrigger extends BaseEntity {

    private static final int MIN_REPEAT_COUNT = 0;
    private static final int MIN_REPEAT_INTERVAL = 1;

    private String triggerName;
    private String triggerGroup;
    private Long repeatInterval;
    private Integer repeatCount;

    public static JobSimpleTrigger of(String triggerName, String triggerGroup, Long repeatInterval, Integer repeatCount) {
        return new JobSimpleTrigger(triggerName, triggerGroup, repeatInterval, repeatCount);
    }

    private JobSimpleTrigger(String triggerName, String triggerGroup, Long repeatInterval, Integer repeatCount) {
        if (repeatCount < MIN_REPEAT_COUNT || repeatInterval <= MIN_REPEAT_INTERVAL) {
            throw new IllegalArgumentException("repeatCount는 0 이상이어야 하며, repeatInterval은 1 이상이어야 합니다.");
        }
        this.triggerName = triggerName;
        this.triggerGroup = triggerGroup;
        this.repeatInterval = repeatInterval;
        this.repeatCount = repeatCount;
    }

}
