package com.example.quartz.quartz.job.repository;

import com.example.quartz.quartz.job.model.JobDetail;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JobDetailRepository {

    private static final String TENANT_ID = "abcedfg";

    private final JobDetailBaseRepository jobDetailBaseRepository;

    public void save(JobDetail jobDetail) {
        jobDetailBaseRepository.save(TENANT_ID, jobDetail);
    }

    public List<JobDetail> findAll() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchAllQuery());
        return jobDetailBaseRepository.findAll(TENANT_ID, boolQueryBuilder, Sort.unsorted());
    }

}
