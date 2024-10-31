package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.quartz.trigger.model.JobSimpleTrigger;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JobSimpleTriggerRepository {

    private static final String TENANT_ID = "abcdef";
    private static final String TRIGGER_NAME_KEYWORD = "triggerName.keyword";
    private static final String TRIGGER_GROUP_KEYWORD = "triggerGroup.keyword";

    private final JobSimpleTriggerBaseRepository jobSimpleTriggerBaseRepository;

    public JobSimpleTrigger save(JobSimpleTrigger jobSimpleTrigger) {
        return jobSimpleTriggerBaseRepository.save(TENANT_ID, jobSimpleTrigger);
    }

    public Optional<JobSimpleTrigger> findByTriggerNameAndTriggerGroup(String triggerName, String triggerGroup) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(TRIGGER_NAME_KEYWORD, triggerName))
                .filter(QueryBuilders.termQuery(TRIGGER_GROUP_KEYWORD, triggerGroup));
        return Optional.ofNullable(jobSimpleTriggerBaseRepository.find(TENANT_ID, boolQueryBuilder));
    }

}
