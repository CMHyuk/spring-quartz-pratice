package com.example.quartz.quartz;

import com.example.quartz.schedulejob.ScheduleJob;
import com.example.quartz.schedulejob.ScheduleJobRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class QuartzSchedulerConfig {

    private final Scheduler scheduler;
    private final ScheduleJobRepository scheduleJobRepository;

    @PostConstruct
    public void initializeJobSchedules() throws SchedulerException {
        List<ScheduleJob> retrievedScheduleJobs = scheduleJobRepository.findAll();

        Map<JobDetail, Set<? extends Trigger>> scheduleJobs = retrievedScheduleJobs.stream()
                .collect(Collectors.toMap(
                        this::createJobDetail,
                        jobSchedule -> Collections.singleton(createTrigger(jobSchedule))
                ));

        scheduler.scheduleJobs(scheduleJobs, true);
    }

    private JobDetail createJobDetail(ScheduleJob scheduleJob) {
        Class<? extends Job> jobClass = getJobClass(scheduleJob.getJobClass());
        return JobBuilder.newJob(jobClass)
                .withIdentity(scheduleJob.getJobName())
                .build();
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Job class not found: " + jobClassName, e);
        }
    }

    private Trigger createTrigger(ScheduleJob scheduleJob) {
        return TriggerBuilder.newTrigger()
                .withIdentity(scheduleJob.getTriggerName())
                .withSchedule(CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression()))
                .forJob(scheduleJob.getJobName())
                .build();
    }

}
