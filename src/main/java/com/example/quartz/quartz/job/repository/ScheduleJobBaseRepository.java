package com.example.quartz.quartz.job.repository;

import com.example.quartz.elasticsearch.base.CustomAwareRepository;
import com.example.quartz.quartz.job.model.ScheduleJob;

public interface ScheduleJobBaseRepository extends CustomAwareRepository<ScheduleJob, String> {
}
