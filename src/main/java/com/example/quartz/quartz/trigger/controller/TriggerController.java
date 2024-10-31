package com.example.quartz.quartz.trigger.controller;

import com.example.quartz.quartz.trigger.dto.CronExpressionUpdateRequest;
import com.example.quartz.quartz.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TriggerController {

    private final TriggerService triggerService;

    @PostMapping("/cron-expression/{triggerName}/{triggerGroup}")
    public ResponseEntity<Void> updateCronExpression(@PathVariable String triggerName, @PathVariable String triggerGroup, @RequestBody CronExpressionUpdateRequest request) {
        triggerService.updateCronExpression(triggerName, triggerGroup, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/job-trigger/{jobName}/{jobGroup}")
    public ResponseEntity<Void> resumeTrigger(@PathVariable String jobName, @PathVariable String jobGroup) {
        triggerService.triggerJob(jobName, jobGroup);
        return ResponseEntity.ok().build();
    }

}
