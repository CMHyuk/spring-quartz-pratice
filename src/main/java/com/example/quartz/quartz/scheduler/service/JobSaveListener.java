package com.example.quartz.quartz.scheduler.service;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.scheduler.dto.JobSaveMessage;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSaveListener {

    private final Scheduler scheduler;

    @RabbitListener(queues = "#{@uniqueQueueName}")
    public void receiveJobSaveMessage(JobSaveMessage jobSaveMessage) throws SchedulerException {
        ScheduleJob scheduleJob = jobSaveMessage.scheduleJob();
        JobTrigger jobTrigger = jobSaveMessage.jobTrigger();
        JobCronTrigger jobCronTrigger = jobSaveMessage.jobCronTrigger();

        Map<JobDetail, Set<? extends Trigger>> scheduleJobs = Stream.of(scheduleJob)
                .collect(Collectors.toMap(
                        this::createJobDetail,
                        job -> createTriggersForJob(jobTrigger, jobCronTrigger)
                ));

        scheduler.scheduleJobs(scheduleJobs, true);
        log.info("메시지를 수신 받아 새로운 Job 저장");
    }

    private JobDetail createJobDetail(ScheduleJob scheduleJob) {
        Class<? extends Job> jobClass = getJobClass(scheduleJob.getJobClassName());

        return JobBuilder.newJob(jobClass)
                .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                .storeDurably(scheduleJob.isDurable())
                .requestRecovery(scheduleJob.isRequestRecovery())
                .build();
    }

    private Set<Trigger> createTriggersForJob(JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
        return Stream.of(createTrigger(jobTrigger, jobCronTrigger))
                .collect(Collectors.toSet());
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Job class not found: " + jobClassName, e);
        }
    }

    private Trigger createTrigger(JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
        return TriggerBuilder.newTrigger()
                .withIdentity(jobTrigger.getTriggerName(), jobTrigger.getTriggerGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobCronTrigger.getCronExpression())
                        .withMisfireHandlingInstructionFireAndProceed()) // 미스파이어 시 즉시 트리거를 실행하고, 이후 스케줄을 계속 진행
                .forJob(jobTrigger.getJobName())
                .build();
    }

}
