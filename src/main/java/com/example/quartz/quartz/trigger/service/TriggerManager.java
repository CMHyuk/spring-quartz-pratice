package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.repository.ScheduleJobRepository;
import com.example.quartz.quartz.trigger.dto.TriggerUpdateMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerManager {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    private final Scheduler scheduler;
    private final RabbitTemplate rabbitTemplate;
    private final ScheduleJobRepository scheduleJobRepository;

    public void updateTrigger(String triggerName, String triggerGroup, String cronExpression) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
            Trigger oldTrigger = scheduler.getTrigger(triggerKey);

            validateTriggerExists(triggerName, oldTrigger);

            Trigger newTrigger = createTrigger(cronExpression, oldTrigger);

            scheduler.rescheduleJob(triggerKey, newTrigger);
            log.info("스케줄 업데이트 적용: " + cronExpression);

            TriggerUpdateMessage updateMessage = new TriggerUpdateMessage(triggerName, triggerGroup, cronExpression);
            rabbitTemplate.convertAndSend(exchangeName, null, updateMessage);
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void validateTriggerExists(String triggerName, Trigger oldTrigger) throws SchedulerException {
        if (oldTrigger == null) {
            throw new SchedulerException("트리거를 찾을 수 없습니다: " + triggerName);
        }
    }

    private Trigger createTrigger(String cronExpression, Trigger oldTrigger) {
        return TriggerBuilder.newTrigger()
                .withIdentity(oldTrigger.getKey())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionFireAndProceed())
                .forJob(oldTrigger.getJobKey())
                .build();
    }

    public void triggerJob(String jobName, String jobGroup) {
        try {
            ScheduleJob scheduleJob = scheduleJobRepository.findByJobNameAndJobGroup(jobName, jobGroup)
                    .orElseThrow(EntityNotFoundException::new);

            scheduler.triggerJob(JobKey.jobKey(scheduleJob.getJobName(), jobGroup));
        } catch (SchedulerException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
