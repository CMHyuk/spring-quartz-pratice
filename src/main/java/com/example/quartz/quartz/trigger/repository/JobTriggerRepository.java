package com.example.quartz.quartz.trigger.repository;

import com.example.quartz.quartz.trigger.model.JobTrigger;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JobTriggerRepository {

    private static final String TENANT_ID = "abcedfg";
    private static final String JOB_NAME_KEYWORD = "jobName.keyword";

    private final JobTriggerBaseRepository jobTriggerBaseRepository;

    public void save(JobTrigger jobTrigger) {
        jobTriggerBaseRepository.save(TENANT_ID, jobTrigger);
    }

    public List<JobTrigger> findAllByJobName(String jobName) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(JOB_NAME_KEYWORD, jobName));
        return jobTriggerBaseRepository.findAll(TENANT_ID, boolQueryBuilder, Sort.unsorted());
    }

}
