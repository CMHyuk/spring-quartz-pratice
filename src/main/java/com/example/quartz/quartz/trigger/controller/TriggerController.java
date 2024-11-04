package com.example.quartz.quartz.trigger.controller;

import com.example.quartz.quartz.trigger.dto.CronExpressionUpdateRequest;
import com.example.quartz.quartz.trigger.dto.TriggerSaveRequest;
import com.example.quartz.quartz.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TriggerController {

    private final TriggerService triggerService;

    @PatchMapping("/cron-expression/{triggerName}/{triggerGroup}")
    public ResponseEntity<Void> updateCronExpression(@PathVariable String triggerName, @PathVariable String triggerGroup, @RequestBody CronExpressionUpdateRequest request) {
        triggerService.updateCronExpression(triggerName, triggerGroup, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cron-trigger")
    public ResponseEntity<Void> addCronTrigger(@RequestBody TriggerSaveRequest request) {
        triggerService.addCronTrigger(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/job/{jobName}/{jobGroup}/run")
    public ResponseEntity<Void> resumeTrigger(@PathVariable String jobName, @PathVariable String jobGroup) {
        triggerService.triggerJob(jobName, jobGroup);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cron-trigger/{triggerName}/{triggerGroup}")
    public ResponseEntity<Void> deleteCronTrigger(@PathVariable String triggerName, @PathVariable String triggerGroup) {
        triggerService.deleteCronTrigger(triggerName, triggerGroup);
        return ResponseEntity.ok().build();
    }

}
