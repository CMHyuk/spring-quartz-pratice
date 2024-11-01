package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.repository.ScheduleJobRepository;
import com.example.quartz.quartz.trigger.dto.TriggerUpdateMessage;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
import com.example.quartz.quartz.trigger.util.TriggerGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerManager {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    private final Scheduler scheduler;
    private final RabbitTemplate rabbitTemplate;
    private final JobTriggerRepository jobTriggerRepository;
    private final ScheduleJobRepository scheduleJobRepository;

    public void updateTrigger(JobCronTrigger jobCronTrigger) {
        String triggerName = jobCronTrigger.getTriggerName();
        String triggerGroup = jobCronTrigger.getTriggerGroup();

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        buildAndUpdateTrigger(triggerKey, jobCronTrigger);

        publishUpdateMessage(triggerName, triggerGroup, jobCronTrigger.getCronExpression());
        log.info("스케줄 업데이트 적용: {}", jobCronTrigger.getCronExpression());
    }

    public void triggerJob(String jobName, String jobGroup) {
        ScheduleJob scheduleJob = findScheduleJob(jobName, jobGroup);
        executeJob(scheduleJob.getJobName(), jobGroup);
    }

    private void buildAndUpdateTrigger(TriggerKey triggerKey, JobCronTrigger jobCronTrigger) {
        try {
            Trigger oldTrigger = scheduler.getTrigger(triggerKey);
            Trigger newTrigger = TriggerGenerator.createCronTrigger(jobCronTrigger);

            if (oldTrigger == null) {
                JobDetail jobDetail = getJobDetailForNewTrigger(jobCronTrigger);
                scheduler.scheduleJob(jobDetail, newTrigger);
                return;
            }

            scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            throw new IllegalStateException("트리거 업데이트 중 에러가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private JobDetail getJobDetailForNewTrigger(JobCronTrigger jobCronTrigger) {
        JobTrigger jobTrigger = getJobTrigger(jobCronTrigger);
        ScheduleJob scheduleJob = getScheduleJob(jobTrigger);
        return createJobDetail(scheduleJob);
    }

    private JobTrigger getJobTrigger(JobCronTrigger jobCronTrigger) {
        return jobTriggerRepository.findByTriggerNameAndTriggerGroup(
                        jobCronTrigger.getTriggerName(), jobCronTrigger.getTriggerGroup())
                .orElseThrow(() -> new EntityNotFoundException("Trigger를 찾을 수 없습니다."));
    }

    private ScheduleJob getScheduleJob(JobTrigger jobTrigger) {
        return scheduleJobRepository.findByJobNameAndJobGroup(
                        jobTrigger.getJobName(), jobTrigger.getJobGroup())
                .orElseThrow(() -> new EntityNotFoundException("Job을 찾을 수 없습니다."));
    }

    private JobDetail createJobDetail(ScheduleJob scheduleJob) {
        Class<? extends Job> jobClass = getJobClass(scheduleJob.getJobClassName());
        return JobBuilder.newJob(jobClass)
                .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                .storeDurably(scheduleJob.isDurable())
                .requestRecovery(scheduleJob.isRequestRecovery())
                .build();
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Job Class를 찾을 수 없습니다: " + jobClassName, e);
        }
    }

    private void publishUpdateMessage(String triggerName, String triggerGroup, String cronExpression) {
        TriggerUpdateMessage updateMessage = new TriggerUpdateMessage(triggerName, triggerGroup, cronExpression);
        rabbitTemplate.convertAndSend(exchangeName, null, updateMessage);
    }

    private ScheduleJob findScheduleJob(String jobName, String jobGroup) {
        return scheduleJobRepository.findByJobNameAndJobGroup(jobName, jobGroup)
                .orElseThrow(() -> new EntityNotFoundException("스케줄 작업을 찾을 수 없습니다: " + jobName));
    }

    private void executeJob(String jobName, String jobGroup) {
        try {
            scheduler.triggerJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Job 실행 중 에러가 발생했습니다: " + e.getMessage(), e);
        }
    }

}
