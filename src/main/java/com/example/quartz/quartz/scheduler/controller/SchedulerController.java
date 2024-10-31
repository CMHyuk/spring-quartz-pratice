package com.example.quartz.quartz.scheduler.controller;

import com.example.quartz.quartz.scheduler.dto.CronJobSaveRequest;
import com.example.quartz.quartz.scheduler.dto.SimpleJobSaveRequest;
import com.example.quartz.quartz.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

    @PostMapping("/cron-job/register")
    public ResponseEntity<Void> registerCronJob(@RequestBody CronJobSaveRequest request) {
        schedulerService.registerCronJob(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/simple-job/register")
    public ResponseEntity<Void> registerSimpleJob(@RequestBody SimpleJobSaveRequest request) {
        return ResponseEntity.ok().build();
    }

}
