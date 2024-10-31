package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.trigger.dto.TriggerUpdateMessage;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.repository.JobCronTriggerRepository;
import com.example.quartz.quartz.trigger.util.TriggerGenerator;
import jakarta.persistence.EntityNotFoundException;
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
    private final JobCronTriggerRepository jobCronTriggerRepository;

    @RabbitListener(queues = "#{@uniqueQueueName}")
    public void receiveCronTriggerUpdateMessage(TriggerUpdateMessage updateMessage) {
        try {
            processCronTriggerUpdate(updateMessage);
            log.info("큐에서 메세지를 수신 받아 스케줄 업데이트 적용: {}", updateMessage.cronExpression());
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 업데이트 오류: " + e.getMessage(), e);
        }
    }

    private void processCronTriggerUpdate(TriggerUpdateMessage updateMessage) throws SchedulerException {
        String triggerName = updateMessage.triggerName();
        String triggerGroup = updateMessage.triggerGroup();

        TriggerKey triggerKey = TriggerKey.triggerKey(updateMessage.triggerName(), updateMessage.triggerGroup());
        Trigger oldTrigger = findExistingTrigger(triggerKey);

        JobCronTrigger jobCronTrigger = jobCronTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .orElseThrow(EntityNotFoundException::new);

        Trigger newTrigger = TriggerGenerator.createCronTrigger(jobCronTrigger, oldTrigger.getJobKey().getName());
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }

    private Trigger findExistingTrigger(TriggerKey triggerKey) throws SchedulerException {
        Trigger oldTrigger = scheduler.getTrigger(triggerKey);
        if (oldTrigger == null) {
            throw new SchedulerException("트리거를 찾을 수 없습니다: " + triggerKey.getName());
        }
        return oldTrigger;
    }

}
