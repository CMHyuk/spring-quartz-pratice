package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.trigger.dto.TriggerUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TriggerUpdateListener {

    private final Scheduler scheduler;

    public TriggerUpdateListener(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @RabbitListener(queues = "#{@uniqueQueueName}")
    public void receiveTriggerUpdate(TriggerUpdateMessage updateMessage) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(updateMessage.triggerName(), updateMessage.triggerGroup());
            Trigger oldTrigger = scheduler.getTrigger(triggerKey);

            validateTriggerExists(updateMessage, oldTrigger);

            Trigger newTrigger = createTrigger(updateMessage.cronExpression(), oldTrigger);

            scheduler.rescheduleJob(triggerKey, newTrigger);
            log.info("큐에서 메세지를 수신 받아 스케줄 업데이트 적용: " + updateMessage.cronExpression());
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 업데이트 오류: " + e.getMessage());
        }
    }

    private void validateTriggerExists(TriggerUpdateMessage updateMessage, Trigger oldTrigger) throws SchedulerException {
        if (oldTrigger == null) {
            throw new SchedulerException("트리거를 찾을 수 없습니다: " + updateMessage.triggerName());
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

}
