package com.example.quartz.quartz.trigger.service;

import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.repository.ScheduleJobRepository;
import com.example.quartz.quartz.trigger.dto.TriggerUpdateMessage;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import com.example.quartz.quartz.trigger.model.JobTrigger;
import com.example.quartz.quartz.trigger.repository.JobCronTriggerRepository;
import com.example.quartz.quartz.trigger.repository.JobTriggerRepository;
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
    private final JobTriggerRepository jobTriggerRepository;
    private final ScheduleJobRepository scheduleJobRepository;

    @RabbitListener(queues = "#{@uniqueQueueName}")
    public void receiveCronTriggerUpdateMessage(TriggerUpdateMessage updateMessage) {
        try {
            processCronTriggerUpdate(updateMessage);
            log.info("큐에서 메세지를 수신 받아 스케줄 업데이트 적용: {}", updateMessage.cronExpression());
        } catch (SchedulerException e) {
            throw new IllegalStateException("스케줄 업데이트 오류: " + e.getMessage(), e);
        }
    }

    private void processCronTriggerUpdate(TriggerUpdateMessage updateMessage) throws SchedulerException {
        String triggerName = updateMessage.triggerName();
        String triggerGroup = updateMessage.triggerGroup();

        JobCronTrigger jobCronTrigger = jobCronTriggerRepository.findByTriggerNameAndTriggerGroup(triggerName, triggerGroup)
                .orElseThrow(EntityNotFoundException::new);

        Trigger newTrigger = TriggerGenerator.createCronTrigger(jobCronTrigger);

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
        Trigger oldTrigger = scheduler.getTrigger(triggerKey);
        if (oldTrigger == null) {
            JobTrigger jobTrigger = jobTriggerRepository.findByTriggerNameAndTriggerGroup(jobCronTrigger.getTriggerName(), jobCronTrigger.getTriggerGroup())
                    .orElseThrow();

            ScheduleJob scheduleJob = scheduleJobRepository.findByJobNameAndJobGroup(jobTrigger.getJobName(), jobTrigger.getJobGroup())
                    .orElseThrow();

            JobDetail jobDetail = createJobDetail(scheduleJob);
            scheduler.scheduleJob(jobDetail, newTrigger);
            return;
        }

        scheduler.rescheduleJob(triggerKey, newTrigger);
    }

    private JobDetail createJobDetail(ScheduleJob scheduleJob) {
        Class<? extends Job> jobClass = getJobClass(scheduleJob.getJobClassName());
        return JobBuilder.newJob(jobClass)
                .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                .storeDurably(scheduleJob.isDurable())
                .requestRecovery(scheduleJob.isRequestRecovery())
                .build();
    }

    private Class<? extends Job> getJobClass(String jobClassName) {
        try {
            return (Class<? extends Job>) Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Job Class를 찾을 수 없습니다: " + jobClassName, e);
        }
    }

}
