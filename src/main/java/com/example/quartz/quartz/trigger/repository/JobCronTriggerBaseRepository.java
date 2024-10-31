package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.elasticsearch.base.CustomAwareRepository;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;

public interface JobCronTriggerBaseRepository extends CustomAwareRepository<JobCronTrigger, String> {
}
