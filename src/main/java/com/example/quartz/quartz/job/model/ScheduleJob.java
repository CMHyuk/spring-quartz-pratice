package com.example.quartz.quartz.job.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

@Entity
@Getter
@Document(indexName = "job_schedule_" + "*", createIndex = false)
@Setting(settingPath = "lower_case_normalizer_setting.json")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleJob {

    @Id
    private String id;

    private String jobName;
    private String jobGroup;
    private String jobClassName;
    private boolean isDurable; // true로 설정된 Job은 트리거가 제거되어도 스케줄러에 유지되며, 언제든지 새로운 트리거를 추가하여 재실행 가능, false는 트리거가 제거시 Job도 스케줄러에서 제거
    private boolean requestRecovery; // 장애 발생 시 복구를 요청하는지 여부

    public static ScheduleJob of(String jobName, String jobGroup, String jobClassName, boolean isDurable, boolean requestRecovery) {
        return new ScheduleJob(jobName, jobGroup, jobClassName, isDurable, requestRecovery);
    }

    private ScheduleJob(String jobName, String jobGroup, String jobClassName, boolean isDurable, boolean requestRecovery) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.jobClassName = jobClassName;
        this.isDurable = isDurable;
        this.requestRecovery = requestRecovery;
    }

}
