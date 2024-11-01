package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.quartz.trigger.model.JobTrigger;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JobTriggerRepository {

    private static final String TENANT_ID = "abcedfg";
    private static final String JOB_NAME_KEYWORD = "jobName.keyword";
    private static final String JOB_GROUP_KEYWORD = "jobGroup.keyword";
    private static final String TRIGGER_NAME_KEYWORD = "triggerName.keyword";
    private static final String TRIGGER_GROUP_KEYWORD = "triggerGroup.keyword";

    private final JobTriggerBaseRepository jobTriggerBaseRepository;

    public JobTrigger save(JobTrigger jobTrigger) {
        return jobTriggerBaseRepository.save(TENANT_ID, jobTrigger);
    }

    public List<JobTrigger> findAllByJobNameJobGroup(String jobName, String jobGroup) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(JOB_NAME_KEYWORD, jobName))
                .filter(QueryBuilders.termQuery(JOB_GROUP_KEYWORD, jobGroup));
        return jobTriggerBaseRepository.findAll(TENANT_ID, boolQueryBuilder, Sort.unsorted());
    }

    public Optional<JobTrigger> findByTriggerNameAndTriggerGroup(String triggerName, String triggerGroup) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(TRIGGER_NAME_KEYWORD, triggerName))
                .filter(QueryBuilders.termQuery(TRIGGER_GROUP_KEYWORD, triggerGroup));
        return Optional.ofNullable(jobTriggerBaseRepository.find(TENANT_ID, boolQueryBuilder));
    }

}
