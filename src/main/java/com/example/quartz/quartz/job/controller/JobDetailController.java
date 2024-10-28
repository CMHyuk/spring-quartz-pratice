package com.example.quartz.quartz.job.controller;

import com.example.quartz.quartz.job.dto.JobDetailSaveRequest;
import com.example.quartz.quartz.job.service.JobDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JobDetailController {

    private final JobDetailService jobDetailService;

    @PostMapping("/job-detail")
    public ResponseEntity<Void> save(@RequestBody JobDetailSaveRequest request) {
        jobDetailService.save(request);
        return ResponseEntity.ok().build();
    }

}
