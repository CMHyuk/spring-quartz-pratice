package com.example.quartz.quartz;

import com.example.quartz.schedulejob.ScheduleJob;
import com.example.quartz.schedulejob.ScheduleJobRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuartzSchedulerController {

    private final ScheduleJobRepository scheduleJobRepository;
    private final Scheduler scheduler;

    @PostMapping("/quartz-scheduler")
    public void changeScheduleJob(@RequestParam String triggerName, @RequestBody UpdateCronExpressionRequest request) throws SchedulerException {
        ScheduleJob scheduleJob = scheduleJobRepository.findByTriggerName(triggerName)
                .orElseThrow(EntityNotFoundException::new);

        scheduleJob.updateCronExpression(request.cronExpression());

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, "DEFAULT");

        Trigger oldTrigger = scheduler.getTrigger(triggerKey);
        if (oldTrigger == null) {
            throw new SchedulerException("트리거를 찾을 수 없습니다: " + triggerName);
        }

        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(oldTrigger.getKey())
                .withSchedule(CronScheduleBuilder.cronSchedule(request.cronExpression()).withMisfireHandlingInstructionDoNothing())

                .forJob(oldTrigger.getJobKey())
                .build();

        scheduleJobRepository.save(scheduleJob);
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }

}
