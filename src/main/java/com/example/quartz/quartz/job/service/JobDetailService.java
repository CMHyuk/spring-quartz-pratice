package com.example.quartz.quartz.job.service;

import com.example.quartz.quartz.job.dto.JobDetailSaveRequest;
import com.example.quartz.quartz.job.model.JobDetail;
import com.example.quartz.quartz.job.repository.JobDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobDetailService {

    private final JobDetailRepository jobDetailRepository;

    public void save(JobDetailSaveRequest request) {
        JobDetail jobDetail = request.toJobSchedule();
        jobDetailRepository.save(jobDetail);
    }

}
