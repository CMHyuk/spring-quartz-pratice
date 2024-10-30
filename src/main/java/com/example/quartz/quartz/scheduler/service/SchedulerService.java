package com.example.quartz.quartz.scheduler.service;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.service.ScheduleJobService;
import com.example.quartz.quartz.scheduler.dto.JobSaveMessage;
import com.example.quartz.quartz.scheduler.dto.JobSaveRequest;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public void registerJob(JobSaveRequest request) {
        ScheduleJob scheduleJob = scheduleJobService.saveJobDetail(request.scheduleJobSaveRequest());
        JobTrigger jobTrigger = triggerService.saveJobTrigger(request.jobTriggerSaveRequest());
        JobCronTrigger jobCronTrigger = triggerService.saveCronTrigger(request.cronTriggerSaveRequest());

        Map<JobDetail, Set<? extends Trigger>> scheduleJobs = createScheduleJobs(scheduleJob, jobTrigger, jobCronTrigger);

        try {
            scheduler.scheduleJobs(scheduleJobs, true);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage());
        }

        publishToQueue(scheduleJob, jobTrigger, jobCronTrigger);
        log.info("새로운 Job을 저장하고 큐에 메시지 전송");
    }

    private Map<JobDetail, Set<? extends Trigger>> createScheduleJobs(ScheduleJob scheduleJob, JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
        return Stream.of(scheduleJob)
                .collect(Collectors.toMap(
                        this::createJobDetail,
                        job -> createTriggersForJob(jobTrigger, jobCronTrigger)
                ));
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

    private void publishToQueue(ScheduleJob scheduleJob, JobTrigger jobTrigger, JobCronTrigger jobCronTrigger) {
        JobSaveMessage jobSaveMessage = new JobSaveMessage(scheduleJob, jobTrigger, jobCronTrigger);
        rabbitTemplate.convertAndSend(exchangeName, null, jobSaveMessage);
    }

}
