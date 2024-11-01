package com.example.quartz.quartz.config;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.repository.ScheduleJobRepository;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
import com.example.quartz.quartz.trigger.service.TriggerFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final Scheduler scheduler;
    private final ScheduleJobRepository scheduleJobRepository;
    private final JobTriggerRepository jobTriggerRepository;
    private final TriggerFactory triggerFactory;

    @PostConstruct
    public void initializeJobSchedules() {
        List<ScheduleJob> jobDetails = scheduleJobRepository.findAll();

        Map<JobDetail, Set<? extends Trigger>> scheduleJobs = jobDetails.stream()
                .collect(Collectors.toMap(
                        this::createJobDetail,
                        this::createTriggersForJob
                ));

        scheduleJobs.entrySet().forEach(this::scheduleJobWithTriggers);
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
        List<JobTrigger> jobTriggers = jobTriggerRepository.findAllByJobNameJobGroup(scheduleJob.getJobName(), scheduleJob.getJobGroup());

        return jobTriggers.stream()
                .map(this::createTrigger)
                .collect(Collectors.toSet());
    }

    private void scheduleJobWithTriggers(Map.Entry<JobDetail, Set<? extends Trigger>> jobDetailEntry) {
        JobDetail jobDetail = jobDetailEntry.getKey();
        Set<? extends Trigger> triggers = jobDetailEntry.getValue();
        triggers.forEach(trigger -> scheduleJob(jobDetail, trigger));
    }

    private void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Scheduler error for job: {} with trigger: {}", jobDetail.getKey(), trigger.getKey(), e);
        }
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Job class not found: " + jobClassName, e);
        }
    }

    private Trigger createTrigger(JobTrigger jobTrigger) {
        return triggerFactory.createTrigger(jobTrigger);
    }

}
