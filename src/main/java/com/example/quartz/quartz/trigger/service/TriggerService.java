package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.trigger.dto.CronExpressionUpdateRequest;
import com.example.quartz.quartz.trigger.dto.CronTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.JobTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.TriggerSaveRequest;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobCronTriggerRepository;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
import com.example.quartz.quartz.trigger.util.CronExpressionGenerator;
import com.example.quartz.quartz.trigger.util.Frequency;
import com.example.quartz.quartz.trigger.util.TriggerGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerService {

    private final Scheduler scheduler;
    private final TriggerManager triggerManager;
    private final JobTriggerRepository jobTriggerRepository;
    private final JobCronTriggerRepository jobCronTriggerRepository;

    public void addCronTrigger(TriggerSaveRequest request) {
        try {
            JobTrigger jobTrigger = this.saveJobTrigger(request.jobTriggerSaveRequest());
            JobCronTrigger jobCronTrigger = this.saveCronTrigger(request.cronTriggerSaveRequest());

            Trigger cronTrigger = TriggerGenerator.createCronTrigger(jobTrigger.getJobName(), jobTrigger.getJobGroup(), jobCronTrigger);
            scheduler.scheduleJob(cronTrigger);

            log.info("새로운 Trigger를 저장하고 큐에 메시지 전송");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public JobTrigger saveJobTrigger(JobTriggerSaveRequest request) {
        JobTrigger jobTrigger = request.toTriggerSchedule();
        return jobTriggerRepository.save(jobTrigger);
    }

    public JobCronTrigger saveCronTrigger(CronTriggerSaveRequest request) {
        String cronExpression = generateCronExpression(request.cronExpressionRequest());
        JobCronTrigger jobCronTrigger = request.toCronTrigger(cronExpression);

        return jobCronTriggerRepository.save(jobCronTrigger);
    }

    public void updateCronExpression(String triggerName, String triggerGroup, CronExpressionUpdateRequest updateRequest) {
        jobCronTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .ifPresentOrElse(
                        jobCronTrigger -> {
                            String updateCronExpression = generateCronExpression(updateRequest);
                            updateAndSaveTrigger(jobCronTrigger, updateCronExpression);
                            triggerManager.updateCronTrigger(jobCronTrigger);
                        },
                        () -> {
                            throw new EntityNotFoundException("Trigger not found");
                        }
                );
    }

    public void deleteCronTrigger(String triggerName, String triggerGroup) {
        jobTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .ifPresent(jobTriggerRepository::delete);

        jobCronTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .ifPresent(jobCronTriggerRepository::delete);

        triggerManager.deleteCronTrigger(triggerName, triggerGroup);
    }

    public void triggerJob(String jobName, String jobGroup) {
        triggerManager.triggerJob(jobName, jobGroup);
    }

    private String generateCronExpression(CronExpressionUpdateRequest request) {
        Frequency frequency = Frequency.from(request.frequency());
        List<String> selectedDays = frequency.getSelectedDays(request.daysOfWeek(), request.daysOfMonth());
        return CronExpressionGenerator.generateCronExpression(frequency, selectedDays, request.getHour(), request.getMinute(), request.specificDate());
    }

    private void updateAndSaveTrigger(JobCronTrigger jobCronTrigger, String cronExpression) {
        jobCronTrigger.updateCronExpression(cronExpression);
        jobCronTriggerRepository.save(jobCronTrigger);
    }

}
