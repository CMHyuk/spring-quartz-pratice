package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.quartz.trigger.model.JobCronTrigger;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JobCronTriggerRepository {

    private static final String TENANT_ID = "abcedfg";
    private static final String TRIGGER_NAME_KEYWORD = "triggerName.keyword";
    private static final String TRIGGER_GROUP_KEYWORD = "triggerGroup.keyword";

    private final JobCronTriggerBaseRepository jobCronTriggerBaseRepository;

    public JobCronTrigger save(JobCronTrigger jobCronTrigger) {
        return jobCronTriggerBaseRepository.save(TENANT_ID, jobCronTrigger);
    }

    public void delete(JobCronTrigger jobCronTrigger) {
        jobCronTriggerBaseRepository.delete(TENANT_ID, jobCronTrigger);
    }

    public Optional<JobCronTrigger> findByTriggerNameAndTriggerGroup(String triggerName, String triggerGroup) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(TRIGGER_NAME_KEYWORD, triggerName))
                .filter(QueryBuilders.termQuery(TRIGGER_GROUP_KEYWORD, triggerGroup));
        return Optional.ofNullable(jobCronTriggerBaseRepository.find(TENANT_ID, boolQueryBuilder));
    }

}
