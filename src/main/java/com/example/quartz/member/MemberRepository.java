package com.example.quartz.member;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private static final String TENANT_ID = "abcedfg";

    private final MemberBaseRepository memberBaseRepository;

    public void save() {
        memberBaseRepository.save(TENANT_ID, new Member("멤버"));
    }

    public void deleteAll() {
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchAllQuery());
        List<Member> members = memberBaseRepository.findAll(TENANT_ID, query, Sort.unsorted());
        memberBaseRepository.deleteAll(TENANT_ID, members);
    }

}
