package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.trigger.dto.TriggerUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerUpdateListener {

    private final Scheduler scheduler;

    @RabbitListener(queues = "#{@uniqueQueueName}")
    public void receiveTriggerUpdateMessage(TriggerUpdateMessage updateMessage) {
        try {
            processTriggerUpdate(updateMessage);
            log.info("큐에서 메세지를 수신 받아 스케줄 업데이트 적용: {}", updateMessage.cronExpression());
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 업데이트 오류: " + e.getMessage(), e);
        }
    }

    private void processTriggerUpdate(TriggerUpdateMessage updateMessage) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(updateMessage.triggerName(), updateMessage.triggerGroup());
        Trigger oldTrigger = findExistingTrigger(updateMessage, triggerKey);

        Trigger newTrigger = buildUpdatedTrigger(updateMessage.cronExpression(), oldTrigger);
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }

    private Trigger findExistingTrigger(TriggerUpdateMessage updateMessage, TriggerKey triggerKey) throws SchedulerException {
        Trigger oldTrigger = scheduler.getTrigger(triggerKey);
        if (oldTrigger == null) {
            throw new SchedulerException("트리거를 찾을 수 없습니다: " + updateMessage.triggerName());
        }
        return oldTrigger;
    }

    private Trigger buildUpdatedTrigger(String cronExpression, Trigger oldTrigger) {
        return TriggerBuilder.newTrigger()
                .withIdentity(oldTrigger.getKey())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionFireAndProceed())
                .forJob(oldTrigger.getJobKey())
                .build();
    }

}
