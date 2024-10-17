package com.example.quartz.schedulejob;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScheduleJobController {

    private final ScheduleJobRepository scheduleJobRepository;

    @PostMapping("/schedule-job")
    public void save(@RequestBody ScheduleJobRequest request) {
        scheduleJobRepository.save(request.toScheduleJob());
    }

    @GetMapping("/schedule-jobs")
    public List<ScheduleJob> findAll() {
        return scheduleJobRepository.findAll();
    }

}
