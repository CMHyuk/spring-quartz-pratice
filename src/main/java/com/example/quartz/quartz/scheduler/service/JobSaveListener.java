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

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSaveListener {

    private final Scheduler scheduler;

    @RabbitListener(queues = "#{@uniqueQueueName}")
    public void receiveJobSaveMessage(JobSaveMessage jobSaveMessage) {
        try {
            scheduleNewJob(jobSaveMessage);
            log.info("메시지를 수신 받아 새로운 Job 저장");
        } catch (SchedulerException e) {
            throw new RuntimeException("Job 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private void scheduleNewJob(JobSaveMessage jobSaveMessage) throws SchedulerException {
        ScheduleJob scheduleJob = jobSaveMessage.scheduleJob();
        JobTrigger jobTrigger = jobSaveMessage.jobTrigger();
        JobCronTrigger jobCronTrigger = jobSaveMessage.jobCronTrigger();

        Map<JobDetail, Set<? extends Trigger>> scheduleJobs = Map.of(
                createJobDetail(scheduleJob),
                createTriggersForJob(jobTrigger, jobCronTrigger)
        );

        scheduler.scheduleJobs(scheduleJobs, true);
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
        return Set.of(createTrigger(jobTrigger, jobCronTrigger));
    }

    private Trigger createTrigger(JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
        return TriggerBuilder.newTrigger()
                .withIdentity(jobTrigger.getTriggerName(), jobTrigger.getTriggerGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobCronTrigger.getCronExpression())
                        .withMisfireHandlingInstructionFireAndProceed())
                .forJob(jobTrigger.getJobName())
                .build();
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Job class not found: " + jobClassName, e);
        }
    }

}
