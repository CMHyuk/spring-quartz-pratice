package com.example.quartz.quartz.config;

import com.example.quartz.quartz.job.model.JobDetail;
import com.example.quartz.quartz.job.repository.JobDetailRepository;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobCronTriggerRepository;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final Scheduler scheduler;
    private final JobDetailRepository jobDetailRepository;
    private final JobTriggerRepository jobTriggerRepository;
    private final JobCronTriggerRepository jobCronTriggerRepository;

    @PostConstruct
    public void initializeJobSchedules() throws SchedulerException {
        List<JobDetail> jobDetails = jobDetailRepository.findAll();

        Map<org.quartz.JobDetail, Set<? extends Trigger>> scheduleJobs = jobDetails.stream()
                .collect(Collectors.toMap(
                        this::createJobDetail,
                        this::createTriggersForJob
                ));

        scheduler.scheduleJobs(scheduleJobs, true);
    }

    private org.quartz.JobDetail createJobDetail(JobDetail jobDetail) {
        Class<? extends Job> jobClass = getJobClass(jobDetail.getJobClassName());

        return JobBuilder.newJob(jobClass)
                .withIdentity(jobDetail.getJobName(), jobDetail.getJobGroup())
                .storeDurably(jobDetail.isDurable())
                .requestRecovery(jobDetail.isRequestRecovery())
                .build();
    }

    private Set<Trigger> createTriggersForJob(JobDetail jobDetail) {
        List<JobTrigger> jobTriggers = jobTriggerRepository.findAllByJobName(jobDetail.getJobName());

        return jobTriggers.stream()
                .map(this::createTrigger)
                .collect(Collectors.toSet());
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Job class not found: " + jobClassName, e);
        }
    }

    private Trigger createTrigger(JobTrigger jobTrigger) {
        JobCronTrigger jobCronTrigger = jobCronTriggerRepository.findByTriggerGroupAndTriggerName(jobTrigger.getTriggerGroup(), jobTrigger.getTriggerName())
                .orElseThrow(EntityNotFoundException::new);

        return TriggerBuilder.newTrigger()
                .withIdentity(jobTrigger.getTriggerName(), jobTrigger.getTriggerGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobCronTrigger.getCronExpression())
                        .withMisfireHandlingInstructionFireAndProceed()) // 미스파이어 시 즉시 트리거를 실행하고, 이후 스케줄을 계속 진행
                .forJob(jobTrigger.getJobName())
                .build();
    }

}
