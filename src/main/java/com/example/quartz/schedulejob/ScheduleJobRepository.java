package com.example.quartz.schedulejob;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduleJobRepository {

    private static final String TENANT_ID = "abcedfg";
    private static final String TRIGGER_NAME_KEYWORD = "triggerName.keyword";

    private final ScheduleJobBaseRepository scheduleJobBaseRepository;

    public List<ScheduleJob> findAll() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchAllQuery());
        return scheduleJobBaseRepository.findAll(TENANT_ID, boolQueryBuilder, Sort.sort(ScheduleJob.class));
    }

    public Optional<ScheduleJob> findByTriggerName(String triggerName) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(TRIGGER_NAME_KEYWORD, triggerName));
        return Optional.ofNullable(scheduleJobBaseRepository.find(TENANT_ID, boolQueryBuilder));
    }

    public void save(ScheduleJob scheduleJob) {
        scheduleJobBaseRepository.save(TENANT_ID, scheduleJob);
    }

}
