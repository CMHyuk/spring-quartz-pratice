package com.example.quartz.quartz;

import com.example.quartz.schedulejob.ScheduleJob;
import com.example.quartz.schedulejob.ScheduleJobService;
import com.example.quartz.schedulejob.UpdateCronExpressionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuartzSchedulerController {

    private final ScheduleJobService scheduleJobService;
    private final TriggerManager triggerManager;

    @PostMapping("/quartz-trigger")
    public void changeScheduleJob(@RequestParam String triggerName, @RequestBody UpdateCronExpressionRequest request) {
        String cronExpression = request.cronExpression();

        ScheduleJob scheduleJob = scheduleJobService.updateCronExpression(triggerName, cronExpression);
        scheduleJobService.save(scheduleJob);

        triggerManager.updateTrigger(triggerName, cronExpression);
    }

}
