package com.example.quartz.schedulejob;

import com.example.quartz.elasticsearch.base.CustomAwareRepository;

public interface ScheduleJobBaseRepository extends CustomAwareRepository<ScheduleJob, String>  {
}
