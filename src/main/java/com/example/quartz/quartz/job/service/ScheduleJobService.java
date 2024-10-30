package com.example.quartz.quartz.job.service;

import com.example.quartz.quartz.job.dto.ScheduleJobSaveRequest;
import com.example.quartz.quartz.job.model.ScheduleJob;
import com.example.quartz.quartz.job.repository.ScheduleJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleJobService {

    private final ScheduleJobRepository scheduleJobRepository;

    public ScheduleJob saveJobDetail(ScheduleJobSaveRequest request) {
        ScheduleJob scheduleJob = request.toJobSchedule();
        return scheduleJobRepository.save(scheduleJob);
    }

}
