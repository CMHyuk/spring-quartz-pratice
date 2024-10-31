package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.trigger.dto.CronExpressionUpdateRequest;
import com.example.quartz.quartz.trigger.dto.CronTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.JobTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.SimpleTriggerSaveRequest;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobSimpleTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobCronTriggerRepository;
import com.example.quartz.quartz.trigger.repository.JobSimpleTriggerRepository;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
import com.example.quartz.quartz.trigger.util.CronExpressionGenerator;
import com.example.quartz.quartz.trigger.util.Frequency;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final TriggerManager triggerManager;
    private final JobTriggerRepository jobTriggerRepository;
    private final JobCronTriggerRepository jobCronTriggerRepository;
    private final JobSimpleTriggerRepository jobSimpleTriggerRepository;

    public JobTrigger saveJobTrigger(JobTriggerSaveRequest request) {
        JobTrigger jobTrigger = request.toTriggerSchedule();
        return jobTriggerRepository.save(jobTrigger);
    }

    public JobCronTrigger saveCronTrigger(CronTriggerSaveRequest request) {
        String cronExpression = generateCronExpression(request.cronExpressionRequest());
        JobCronTrigger jobCronTrigger = request.toCronTrigger(cronExpression);

        return jobCronTriggerRepository.save(jobCronTrigger);
    }

    public JobSimpleTrigger saveSimpleTrigger(SimpleTriggerSaveRequest request) {
        JobSimpleTrigger jobSimpleTrigger = request.toSimpleTrigger();
        return jobSimpleTriggerRepository.save(jobSimpleTrigger);
    }

    public void updateCronExpression(String triggerName, String triggerGroup, CronExpressionUpdateRequest request) {
        JobCronTrigger jobCronTrigger = findJobCronTrigger(triggerName, triggerGroup);
        String cronExpression = generateCronExpression(request);

        updateAndSaveTrigger(jobCronTrigger, cronExpression);
        triggerManager.updateTrigger(triggerName, triggerGroup, cronExpression);
    }

    private JobCronTrigger findJobCronTrigger(String triggerName, String triggerGroup) {
        return jobCronTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .orElseThrow(() -> new EntityNotFoundException("Trigger not found: " + triggerName + ", " + triggerGroup));
    }

    private String generateCronExpression(CronExpressionUpdateRequest request) {
        Frequency frequency = Frequency.from(request.frequency());
        List<String> selectedDays = frequency.getSelectedDays(request.daysOfWeek(), request.daysOfMonth());
        return CronExpressionGenerator.generateCronExpression(
                frequency, selectedDays, request.getHour(), request.getMinute(), request.specificDate());
    }

    private void updateAndSaveTrigger(JobCronTrigger jobCronTrigger, String cronExpression) {
        jobCronTrigger.updateCronExpression(cronExpression);
        jobCronTriggerRepository.save(jobCronTrigger);
    }

    public void triggerJob(String jobName, String jobGroup) {
        triggerManager.triggerJob(jobName, jobGroup);
    }

}
