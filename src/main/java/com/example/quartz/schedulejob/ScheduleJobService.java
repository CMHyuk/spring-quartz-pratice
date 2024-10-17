package com.example.quartz.schedulejob;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleJobService {

    private final ScheduleJobRepository scheduleJobRepository;

    public ScheduleJob updateCronExpression(String triggerName, String cronExpression) {
        ScheduleJob scheduleJob = scheduleJobRepository.findByTriggerName(triggerName)
                .orElseThrow(EntityNotFoundException::new);
        scheduleJob.updateCronExpression(cronExpression);
        return scheduleJob;
    }
}
