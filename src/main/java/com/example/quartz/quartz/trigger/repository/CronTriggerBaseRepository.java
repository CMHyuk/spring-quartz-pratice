package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.elasticsearch.base.CustomAwareRepository;
import com.example.quartz.quartz.trigger.model.JobCronTrigger;

public interface CronTriggerBaseRepository extends CustomAwareRepository<JobCronTrigger, String> {
}
