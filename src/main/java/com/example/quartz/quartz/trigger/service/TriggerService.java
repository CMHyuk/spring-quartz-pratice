package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.trigger.dto.CronTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.JobTriggerSaveRequest;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobCronTriggerRepository;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final TriggerManager triggerManager;
    private final JobTriggerRepository jobTriggerRepository;
    private final JobCronTriggerRepository jobCronTriggerRepository;

    public void saveJobTrigger(JobTriggerSaveRequest request) {
        JobTrigger jobTrigger = request.toTriggerSchedule();
        jobTriggerRepository.save(jobTrigger);
    }

    public void saveCronTrigger(CronTriggerSaveRequest request) {
        JobCronTrigger jobCronTrigger = request.toCronTrigger();
        jobCronTriggerRepository.save(jobCronTrigger);
    }

    public void updateCronExpression(String triggerName, String triggerGroup, String cronExpression) {
        JobCronTrigger jobCronTrigger = jobCronTriggerRepository.findByTriggerGroupAndTriggerName(triggerGroup, triggerName)
                .orElseThrow(EntityNotFoundException::new);

        jobCronTrigger.updateCronExpression(cronExpression);

        jobCronTriggerRepository.save(jobCronTrigger);
        triggerManager.updateTrigger(triggerName, triggerGroup, cronExpression);
    }

    public void triggerJob(String jobName, String jobGroup) {
        triggerManager.triggerJob(jobName, jobGroup);
    }

}
