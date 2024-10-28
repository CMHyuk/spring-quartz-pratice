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

    private final CronTriggerBaseRepository cronTriggerBaseRepository;

    public void save(JobCronTrigger jobCronTrigger) {
        cronTriggerBaseRepository.save(TENANT_ID, jobCronTrigger);
    }

    public Optional<JobCronTrigger> findByTriggerName(String triggerName) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(TRIGGER_NAME_KEYWORD, triggerName));
        return Optional.ofNullable(cronTriggerBaseRepository.find(TENANT_ID, boolQueryBuilder));
    }

}
