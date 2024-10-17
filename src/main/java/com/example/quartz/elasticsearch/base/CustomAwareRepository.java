/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : CustomAwareRepository
 * author: hyunwoo.song
 * description: 모든 Repository 에서 사용할 수 있는 공통 기능을 제공한다.
 * 규칙 1 : 모든 Repository 는 아래의 인터페이스를 상속 받는다.
 * 규칙 2 : ElasticsearchRepository 는 아래의 인터페이스가 상속받으므로, 모든 Repository 에서는 CustomAwareRepository 인터페이스만 상속받는다.
 * 규칙 3 : 각 Repository 에서 커스텀하게 구현해야 할 기능의 경우, 각 Repository 에서 커스텀 인터페이스 및 구현체를 구현해야 한다.
 */

package com.example.quartz.elasticsearch.base;


import com.example.quartz.elasticsearch.dto.SearchAfterDetailRequestDto;
import com.example.quartz.elasticsearch.dto.SearchAfterRequestDto;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;


@NoRepositoryBean
@SuppressWarnings("squid:S119") // ID를 다른 이름으로 변경하라는 워닝을 무시
public interface CustomAwareRepository<T, ID> extends Repository<T, ID> {


    /**
     * @param companyId 회사 아이디
     * @param entity    T 객체 엔티티
     * @param <S>       extends T
     * @return 저장이 완료된 객체를 리턴
     */
    <S extends T> S save(String companyId, S entity);


    /**
     * @param companyId 회사 아이디
     * @param entities  T 객체 엔티티 리스트
     * @param <S>       extends T
     * @return 저장이 완료된 객체 리스트를 리턴
     */
    <S extends T> List<S> saveAll(String companyId, List<S> entities);


    /**
     * @param companyId 회사 아이디
     * @param entity    지울 T 객체 엔티티
     */
    void delete(String companyId, T entity);


    /**
     * @param companyId 회사 아이디
     * @param entities  지울 객체 T 리스트
     *                  Id 를 기반으로 지우기 떄문에 Entity 클라스애서 @Id 를 JsonIgnore 하지 말아야 한다.
     *                  Entity 클래스를 컨트롤러에서 바로 넘기는 것은 지양해야 하며 DTO로 감싸서 숨길 필드를 걸러내야 한다.
     */
    void deleteAll(String companyId, Iterable<? extends T> entities);


    /**
     * @param companyId 회사 아이디
     * @param query     불쿼리를 입력한다.
     * @return T 객체
     */
    T find(String companyId, BoolQueryBuilder query);


    /**
     * findAll(String companyId, BoolQueryBuilder query, List<QueryBuilder> filters, Sort sort, int pageSize)와 동일하지만
     * pageSize 를 기본 1_000으로 넣기 위한 래핑된 함수
     *
     * @param companyId 회사 아이디를 넣은 경우 index_{namespace}_{companyIdLowerClase} 에서 도큐먼트를 찾는다.
     *                  회사 아이디를 넣지 않은 경우 index_{namespace}_* 로 찾는다.
     *                  앞의 문자열이 겹치지 않는 company 와 같은 index 에서는 null 로 설정하여 조회할 수 있다. ( T 의 indexName 참고 )
     *                  하지만 앞의 문자열이 겹치는 client 에서는 oauth_client 와 oauth_client_details index 가 존재하기 때문에 섞여서 나올 수 있으므로 주의해야 한다.
     * @param query     불쿼리를 입력한다. 모든 레파지토리에서 쿼리를 관리하도록 설계하였다.
     *                  만약 null 을 넣는다면 { bool : { must : { match_all }}} 로 조회한다.
     *                  만약 null 을 넣는다면 필터를 사용하지 않는다.
     * @param sort      필수로 입력해야 한다.
     *                  엘라스틱에서는 10_000 개 이상을 한번에 조회하지 못하므로
     *                  이 API 는 search after 를 사용하도록 되어있다.
     *                  search after 를 사용할 때는 sort 값이 필수이다.
     * @return T 객체 리스트
     */
    List<T> findAll(String companyId, BoolQueryBuilder query, Sort sort);

    /**
     * @param companyId 회사 아이디를 넣은 경우 index_{namespace}_{companyIdLowerClase} 에서 도큐먼트를 찾는다.
     *                  회사 아이디를 넣지 않은 경우 index_{namespace}_* 로 찾는다.
     *                  앞의 문자열이 겹치지 않는 company 와 같은 index 에서는 null 로 설정하여 조회할 수 있다. ( T 의 indexName 참고 )
     *                  하지만 앞의 문자열이 겹치는 client 에서는 oauth_client 와 oauth_client_details index 가 존재하기 때문에 섞여서 나올 수 있으므로 주의해야 한다.
     * @param query     불쿼리를 입력한다. 모든 레파지토리에서 쿼리를 관리하도록 설계하였다.
     *                  만약 null 을 넣는다면 { bool : { must : { match_all }}} 로 조회한다.
     *                  만약 null 을 넣는다면 필터를 사용하지 않는다.
     * @param sort      필수로 입력해야 한다.
     *                  엘라스틱에서는 10_000 개 이상을 한번에 조회하지 못하므로
     *                  이 API 는 search after 를 사용하도록 되어있다.
     *                  search after 를 사용할 때는 sort 값이 필수이다.
     * @param pageSize  페이징 사이즈
     * @return T 객체 리스트
     */
    List<T> findAll(String companyId, BoolQueryBuilder query, Sort sort, int pageSize);

    /**
     * @param companyId      회사 아이디
     * @param searchAfterDto searchAfter를 요청한 DTO
     * @return SearchHits<T>
     */
    SearchHits<T> searchAfter(String companyId, SearchAfterRequestDto searchAfterDto);


    /**
     * @param companyId      회사 아이디
     * @param searchAfterDto searchAfterDetail을 요청한 DTO
     * @return SearchHits<T>
     */
    SearchHits<T> searchAfterDetail(String companyId, SearchAfterDetailRequestDto searchAfterDto);

    /**
     * @param companyId       회사 아이디
     * @param updateQueryList 특정 필드만 업데이트하기 위한 쿼리
     * @return bool
     */
    boolean bulkUpdate(String companyId, List<UpdateQuery> updateQueryList);
}
