package com.example.quartz.quartz.scheduler.controller;

import com.example.quartz.quartz.scheduler.dto.CronJobSaveRequest;
import com.example.quartz.quartz.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

    @PostMapping("/job/register")
    public ResponseEntity<Void> registerJob(@RequestBody CronJobSaveRequest request) {
        schedulerService.registerJob(request);
        return ResponseEntity.ok().build();
    }

}
