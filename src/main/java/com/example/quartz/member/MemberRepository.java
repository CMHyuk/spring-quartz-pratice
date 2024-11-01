package com.example.quartz.member;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private static final String TENANT_ID = "abcedfg";
    private static final String NAME_KEYWORD = "name.keyword";

    private final MemberBaseRepository memberBaseRepository;

    public void save(Member member) {
        memberBaseRepository.save(TENANT_ID, member);
    }

    public Optional<Member> findByName(String name) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(NAME_KEYWORD, name));
        return Optional.ofNullable(memberBaseRepository.find(TENANT_ID, boolQueryBuilder));
    }

}
