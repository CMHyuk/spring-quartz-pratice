package com.example.quartz.quartz.trigger.controller;

import com.example.quartz.quartz.trigger.dto.CronExpressionUpdateRequest;
import com.example.quartz.quartz.trigger.dto.CronTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.JobTriggerSaveRequest;
import com.example.quartz.quartz.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TriggerController {

    private final TriggerService triggerService;

    @PostMapping("/job-trigger")
    public ResponseEntity<Void> save(@RequestBody JobTriggerSaveRequest request) {
        triggerService.saveJobTrigger(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cron-trigger")
    public ResponseEntity<Void> save(@RequestBody CronTriggerSaveRequest request) {
        triggerService.saveCronTrigger(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cron-expression/{triggerGroup}/{triggerName}")
    public ResponseEntity<Void> updateCronExpression(@PathVariable String triggerGroup, @PathVariable String triggerName, @RequestBody CronExpressionUpdateRequest request) {
        triggerService.updateCronExpression(triggerGroup, triggerName, request.cronExpression());
        return ResponseEntity.ok().build();
    }

}
