package com.example.quartz.quartz.scheduler.service;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.service.ScheduleJobService;
import com.example.quartz.quartz.scheduler.dto.JobSaveMessage;
import com.example.quartz.quartz.scheduler.dto.CronJobSaveRequest;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.service.TriggerService;
import com.example.quartz.quartz.trigger.util.TriggerGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    private final Scheduler scheduler;
    private final RabbitTemplate rabbitTemplate;
    private final TriggerService triggerService;
    private final ScheduleJobService scheduleJobService;

    public void registerJob(CronJobSaveRequest request) {
        ScheduleJob scheduleJob = scheduleJobService.saveJobDetail(request.scheduleJobSaveRequest());
        JobTrigger jobTrigger = triggerService.saveJobTrigger(request.jobTriggerSaveRequest());
        JobCronTrigger jobCronTrigger = triggerService.saveCronTrigger(request.cronTriggerSaveRequest());

        scheduleAndPublish(scheduleJob, jobTrigger, jobCronTrigger);
        log.info("새로운 Job을 저장하고 큐에 메시지 전송");
    }

    private void scheduleAndPublish(ScheduleJob scheduleJob, JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
        try {
            scheduler.scheduleJobs(createScheduleJobs(scheduleJob, jobCronTrigger), true);
            publishJobSaveMessage(scheduleJob, jobTrigger, jobCronTrigger);
        } catch (SchedulerException e) {
            throw new IllegalStateException("저장 실패: " + e.getMessage(), e);
        }
    }

    private Map<JobDetail, Set<? extends Trigger>> createScheduleJobs(ScheduleJob scheduleJob, JobCronTrigger jobCronTrigger) {
        JobDetail jobDetail = createJobDetail(scheduleJob);
        Set<Trigger> triggers = createTriggersForJob(scheduleJob.getJobName(), scheduleJob.getJobGroup(), jobCronTrigger);
        return Map.of(jobDetail, triggers);
    }

    private JobDetail createJobDetail(ScheduleJob scheduleJob) {
        Class<? extends Job> jobClass = getJobClass(scheduleJob.getJobClassName());
        return JobBuilder.newJob(jobClass)
                .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                .storeDurably(scheduleJob.isDurable())
                .requestRecovery(scheduleJob.isRequestRecovery())
                .build();
    }

    private Set<Trigger> createTriggersForJob(String jobName, String jobGroup, JobCronTrigger jobCronTrigger) {
        Trigger trigger = TriggerGenerator.createCronTrigger(jobName, jobGroup, jobCronTrigger);
        return Set.of(trigger);
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Job Class를 찾을 수 없습니다: " + jobClassName, e);
        }
    }

    private void publishJobSaveMessage(ScheduleJob scheduleJob, JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
        JobSaveMessage jobSaveMessage = new JobSaveMessage(scheduleJob, jobTrigger, jobCronTrigger);
        rabbitTemplate.convertAndSend(exchangeName, null, jobSaveMessage);
    }

}
