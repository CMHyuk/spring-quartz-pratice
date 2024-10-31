package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.elasticsearch.base.CustomAwareRepository;
import com.example.quartz.quartz.trigger.model.JobSimpleTrigger;

public interface JobSimpleTriggerBaseRepository extends CustomAwareRepository<JobSimpleTrigger, String> {
}
