package com.example.quartz.quartz.trigger.controller;

import com.example.quartz.quartz.trigger.dto.CronExpressionUpdateRequest;
import com.example.quartz.quartz.trigger.dto.CronTriggerSaveRequest;
import com.example.quartz.quartz.trigger.dto.JobTriggerSaveRequest;
import com.example.quartz.quartz.trigger.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/cron-expression")
    public ResponseEntity<Void> updateCronExpression(@RequestParam String triggerName, @RequestBody CronExpressionUpdateRequest request) {
        triggerService.updateCronExpression(triggerName, request.cronExpression());
        return ResponseEntity.ok().build();
    }

}
