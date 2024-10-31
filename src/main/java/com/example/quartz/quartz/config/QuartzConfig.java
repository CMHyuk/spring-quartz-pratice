package com.example.quartz.quartz.config;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.repository.ScheduleJobRepository;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
import com.example.quartz.quartz.trigger.service.TriggerFactory;
import jakarta.annotation.PostConstruct;
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
    private final ScheduleJobRepository scheduleJobRepository;
    private final JobTriggerRepository jobTriggerRepository;
    private final TriggerFactory triggerFactory;

    @PostConstruct
    public void initializeJobSchedules() throws SchedulerException {
        List<ScheduleJob> jobDetails = scheduleJobRepository.findAll();

        Map<JobDetail, Set<? extends Trigger>> scheduleJobs = jobDetails.stream()
                .collect(Collectors.toMap(
                        this::createJobDetail,
                        this::createTriggersForJob
                ));

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

    private Set<Trigger> createTriggersForJob(ScheduleJob scheduleJob) {
        List<JobTrigger> jobTriggers = jobTriggerRepository.findAllByJobName(scheduleJob.getJobName());

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
        return triggerFactory.createTrigger(jobTrigger);
    }

}
