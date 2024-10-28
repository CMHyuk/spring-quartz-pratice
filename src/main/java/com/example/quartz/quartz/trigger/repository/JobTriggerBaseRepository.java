package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.elasticsearch.base.CustomAwareRepository;
import com.example.quartz.quartz.trigger.model.JobTrigger;

public interface JobTriggerBaseRepository extends CustomAwareRepository<JobTrigger, String> {
}
