package com.example.quartz.quartz.job.repository;

import com.example.quartz.elasticsearch.base.CustomAwareRepository;
import com.example.quartz.quartz.job.model.JobDetail;

public interface JobDetailBaseRepository extends CustomAwareRepository<JobDetail, String> {
}
