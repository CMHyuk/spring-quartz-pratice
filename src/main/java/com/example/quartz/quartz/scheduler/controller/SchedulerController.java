package com.example.quartz.quartz.scheduler.controller;

import com.example.quartz.quartz.scheduler.dto.JobSaveRequest;
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

    @PostMapping("/job/register")
    public ResponseEntity<Void> registerJob(@RequestBody JobSaveRequest request) {
        schedulerService.registerJob(request);
        return ResponseEntity.ok().build();
    }

}
