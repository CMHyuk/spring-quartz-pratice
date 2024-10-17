/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : CustomAwareRepositoryImpl
 * author: hyunwoo.song
 * description: CustomAwareRepository 인터페이스의 구현체
 */

package com.example.quartz.elasticsearch.base;


import com.example.quartz.elasticsearch.config.ElasticSearchIndexAlias;
import com.example.quartz.elasticsearch.config.ElasticSearchSetting;
import com.example.quartz.elasticsearch.dto.SearchAfterDetailRequestDto;
import com.example.quartz.elasticsearch.dto.SearchAfterRequestDto;
import com.example.quartz.elasticsearch.dto.WildCardOperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.BulkFailureException;
import org.springframework.data.elasticsearch.NoSuchIndexException;
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.*;


@SuppressWarnings("squid:S119") // ID를 다른 이름으로 변경하라는 워닝을 무시
@Slf4j
public class CustomAwareRepositoryImpl<T, ID> implements CustomAwareRepository<T, ID> {

    private static final int DEFAULT_PAGE_SIZE = 1_000;

    protected ElasticsearchOperations operations;
    protected IndexOperations indexOperations;
    protected Class<T> entityClass;
    protected ElasticsearchEntityInformation<T, ID> entityInformation;
    private final ElasticSearchSetting elasticSearchSetting;
    private final ElasticSearchIndexAlias elasticSearchIndexAlias;

    /**
     * @param entityInformation       entity class 정보
     * @param operations              elasticsearch operation ( CRUD )
     * @param elasticSearchSetting    ElasticSearchSetting Component
     * @param elasticSearchIndexAlias ElasticSearchIndexAlias Component
     */
    public CustomAwareRepositoryImpl(ElasticsearchEntityInformation<T, ID> entityInformation,
                                     ElasticsearchOperations operations, ElasticSearchSetting elasticSearchSetting, ElasticSearchIndexAlias elasticSearchIndexAlias) {
        this.elasticSearchSetting = elasticSearchSetting;
        this.elasticSearchIndexAlias = elasticSearchIndexAlias;

        // SimpleElasticsearchRepository 를 상속받는 대신 SimpleElasticsearchRepository 에서 저장하는 데이터를 그대로 저장해서 사용한다.
        this.operations = operations;
        this.entityInformation = entityInformation;
        this.entityClass = this.entityInformation.getJavaType();
        this.indexOperations = operations.indexOps(this.entityClass);
    }


    @Override
    public <S extends T> S save(String companyId, S entity) {
        var indexCoordinates = this.getIndexCoordinates(companyId, true);
        var save = this.operations.save(entity, indexCoordinates);
        this.doRefresh(indexCoordinates);
        return save;
    }

    @Override
    public <S extends T> List<S> saveAll(String companyId, List<S> entities) {
        var indexCoordinates = this.getIndexCoordinates(companyId, true);
        var save = this.operations.save(entities, indexCoordinates);
        this.doRefresh(indexCoordinates);
        return (List<S>) save;
    }

    public void delete(String companyId, T entity) {
        var indexCoordinates = this.getIndexCoordinates(companyId, false);
        this.operations.delete(entity, indexCoordinates);
        this.doRefresh(indexCoordinates);
    }

    @Override
    public void deleteAll(String companyId, Iterable<? extends T> entities) {
        List<ID> ids = new ArrayList<>();

        for (T entity : entities) {
            var id = this.extractIdFromBean(entity);
            if (id != null) {
                ids.add(id);
            }
        }

        this.deleteAllById(companyId, ids);
    }

    @Override
    public T find(String companyId, BoolQueryBuilder query) {

        Assert.notNull(query, "query must not be null");

        var indexCoordinates = this.getIndexCoordinates(companyId);

        // 쿼리, 필터, sort 를 조합한다.
        var searchQuery = createNativeSearchQuery(query, false);


        SearchHits<T> searchHits = search(indexCoordinates, searchQuery);
        if (searchHits.isEmpty()) {
            return null;
        }

        return searchHits.getSearchHit(0).getContent();
    }

    @Override
    public List<T> findAll(String companyId, BoolQueryBuilder query, Sort sort) {
        return this.findAll(companyId, query, sort, DEFAULT_PAGE_SIZE);
    }


    @Override
    public List<T> findAll(String companyId, BoolQueryBuilder query, Sort sort, int pageSize) {

        Assert.notNull(sort, "sort must not be null");
        Assert.state(pageSize > 0, "pageSize must greater than 0");

        IndexCoordinates indexCoordinates = getIndexCoordinates(companyId);

        // 쿼리, 필터, sort 를 조합한다.
        var searchQuery = createNativeSearchQuery(query, true);

        // search after 를 위해 page size 와 sort 를 지정한다.
        searchQuery.setPageable(PageRequest.ofSize(pageSize));
        searchQuery.addSort(sort);


        List<SearchHit<T>> searchTotalHitList = new ArrayList<>();
        SearchHits<T> search;

        log.info(searchQuery.getQuery().toString());
        try {
            do {

                search = this.search(indexCoordinates, searchQuery);

                if (search.isEmpty()) {
                    break;
                }

                List<SearchHit<T>> searchHits = search.getSearchHits();
                addSearchAfterToQuery(searchQuery, searchHits);
                searchTotalHitList.addAll(searchHits);


            } while (search.getTotalHits() > searchTotalHitList.size()); // totalHits 가 더 크면 아직 더 받아올 데이터가 존재한다.

        } catch (Exception e) {
            log.error("search exception", e);
        }


        return (List<T>) SearchHitSupport.unwrapSearchHits(searchTotalHitList);

    }

    @Override
    public SearchHits<T> searchAfter(String companyId, SearchAfterRequestDto searchAfterDto) {


        var indexCoordinates = this.getIndexCoordinates(companyId, false);

        var query = QueryBuilders.boolQuery();

        if (!searchAfterDto.getWildcardSearch().isEmpty()) {

            var wildcardQuery = QueryBuilders.boolQuery();

            searchAfterDto.getWildcardSearch().forEach((key, value) -> {
                if (searchAfterDto.getWildcardSearchOperation() == WildCardOperationType.AND) {
                    wildcardQuery.must(QueryBuilders.wildcardQuery(key, MessageFormat.format("*{0}*", value)));
                } else {
                    wildcardQuery.should(QueryBuilders.wildcardQuery(key, MessageFormat.format("*{0}*", value)));
                }
            });

            query.must(wildcardQuery);
        }


        searchAfterDto.getFilter().forEach((key, value) -> {
            if (value instanceof Collection) {
                query.filter(QueryBuilders.termsQuery(key, (Collection<?>) value));
            } else {
                query.filter(QueryBuilders.termQuery(key, value));
            }
        });

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).build();

        searchQuery.setPageable(PageRequest.ofSize(searchAfterDto.getSize()));

        searchAfterDto.getSort().forEach((key, value) -> {
            var direction = Sort.Direction.DESC;
            if (StringUtils.equalsIgnoreCase("asc", value)) {
                direction = Sort.Direction.ASC;
            }
            searchQuery.addSort(Sort.by(direction, key));
        });

        if (!searchAfterDto.getSearchAfter().isEmpty()) {
            searchQuery.setSearchAfter(searchAfterDto.getSearchAfter());
        }


        return search(indexCoordinates, searchQuery);
    }

    @Override
    public SearchHits<T> searchAfterDetail(String companyId, SearchAfterDetailRequestDto searchAfterDto) {

        var indexCoordinates = this.getIndexCoordinates(companyId, false);

        BoolQueryBuilder query = QueryBuilders.boolQuery();

        searchAfterDto.getMust().forEach((key, value) -> {
            if (value instanceof Collection) {
                query.must(QueryBuilders.termsQuery(key, (Collection<?>) value));
            } else {
                query.must(QueryBuilders.termQuery(key, value));
            }
        });

        // should 와 mustNot은 필터로 등록한다.
        BoolQueryBuilder shouldFilter = QueryBuilders.boolQuery();
        searchAfterDto.getShould().forEach((key, value) -> {
            if (value instanceof Collection) {
                shouldFilter.should(QueryBuilders.termsQuery(key, (Collection<?>) value));
            } else {
                shouldFilter.should(QueryBuilders.termQuery(key, value));
            }
        });

        BoolQueryBuilder mustNotFilter = QueryBuilders.boolQuery();
        searchAfterDto.getMustNot().forEach((key, value) -> {
            if (value instanceof Collection) {
                mustNotFilter.mustNot(QueryBuilders.termsQuery(key, (Collection<?>) value));
            } else {
                mustNotFilter.mustNot(QueryBuilders.termQuery(key, value));
            }
        });

        List<QueryBuilder> filters = Arrays.asList(shouldFilter, mustNotFilter);
        filters.forEach(query::filter);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).build();

        // page size
        searchQuery.setPageable(PageRequest.ofSize(searchAfterDto.getSize()));

        // sort order
        searchAfterDto.getSort().forEach((key, value) -> {
            Sort.Direction direction = Sort.Direction.DESC;
            if (StringUtils.equalsIgnoreCase("asc", value)) {
                direction = Sort.Direction.ASC;
            }

            searchQuery.addSort(Sort.by(direction, key));
        });

        // search after
        if (!searchAfterDto.getSearchAfter().isEmpty()) {
            searchQuery.setSearchAfter(searchAfterDto.getSearchAfter());
        }

        return search(indexCoordinates, searchQuery);
    }

    /**
     * 개발용으로 operations.search() 를 호출 할 때 로그를 남길 수 있도록 랩핑한다. 필요에따라 지워야한다.
     *
     * @param indexCoordinates indexCoordinates
     * @param searchQuery      query
     * @return SearchHits<T>
     */
    private SearchHits<T> search(IndexCoordinates indexCoordinates, NativeSearchQuery searchQuery) {

        try {
            return this.operations.search(searchQuery, this.entityClass, indexCoordinates);
        } catch (NoSuchIndexException ex) {
            log.info("elasticsearch index not found : {}", ex.getIndex());

        } catch (UncategorizedElasticsearchException ex) {
            // index만 생성되고 데이터가 맵핑되지 않았을 때
            log.info("elasticsearch uncategorized exception : {}", ex.getMessage());
        }

        return new SearchHitsImpl<>(0, TotalHitsRelation.EQUAL_TO, 0.0f, null, Collections.emptyList(), null);
    }


    @Override
    public boolean bulkUpdate(String companyId, List<UpdateQuery> updateQUeryList) {

        var indexCoordinates = this.getIndexCoordinates(companyId, false);
        try {
            // 현재 bulkUpdate는 이벤트 처리로 백그라운드에서 사용하기 떄문에 익셉션 로깅만 남긴다.
            this.operations.bulkUpdate(updateQUeryList, indexCoordinates);
            this.doRefresh(indexCoordinates);
            return true;
        } catch (BulkFailureException ex) {
            log.error("bulk failure exception : ", ex);
        }
        return false;
    }


    //////////////////////////////////////// private methods ////////////////////////////////////////

    /**
     * indexName_{companyIdLowerCase}에 해당하는 인덱스가 없는 경우 생성
     *
     * @param companyId 회사 아이디
     * @return indexName_{namespace}_{companyIdLowerCase} 규칙의 alias 된 IndexCoordinates 를 리턴
     */
    private IndexCoordinates getIndexCoordinates(String companyId, boolean create) {

        String indexName = this.getIndexNameWithAlias(companyId);
        var indexCoordinates = IndexCoordinates.of(indexName);
        IndexOperations indexOpsWithAlias = this.operations.indexOps(indexCoordinates);

        // 해당 인덱스가 없으면
        if (create && !indexOpsWithAlias.exists()) {

            IndexOperations indexOpsWithoutAlias = this.createIndexWithMapping(companyId);
            this.createAlias(companyId, indexOpsWithoutAlias);
        }

        return indexCoordinates;
    }


    private IndexCoordinates getIndexCoordinates(String companyId) {
        IndexCoordinates indexCoordinates;

        if (StringUtils.isEmpty(companyId)) {
            // companyId 를 지정하지 않은 경우 T에 대한 모든 인덱스
            var indexName = this.getIndexNameWithAliasAndAsterisk();
            indexCoordinates = IndexCoordinates.of(indexName);
        } else {
            // companyId 를 지정한 경우 정확한 T 에 대한 인덱스
            indexCoordinates = this.getIndexCoordinates(companyId, false);
        }
        return indexCoordinates;
    }


    /**
     * @param query 불쿼리를 입력한다. null 이 입력된 경우 match_all 의 쿼리를 사용하도록 한다.
     * @return NativeSearchQuery
     */
    private NativeSearchQuery createNativeSearchQuery(BoolQueryBuilder query, boolean useDefaultQuery) {

        // query 가 null 이면 match_all 을 사용한다.
        if (query == null && useDefaultQuery) {
            query = new BoolQueryBuilder().must(QueryBuilders.matchAllQuery());
        }
        // 위 로직에서 query 가 null 이면 match_all 을 사용하기 떄문에 null 이면 안된다.
        Assert.notNull(query, "query must be not null");


        return new NativeSearchQueryBuilder().withQuery(query).build();
    }

    /**
     * Description: index 를 생성하고 field mapping 을 수행한다.
     * index 생성 규칙은 indexName_{companyIdLowerCase} 이다.
     *
     * @param companyId 회사 아이디
     */
    private IndexOperations createIndexWithMapping(String companyId) {

        // 인덱스 생성을 위해 namespace 가 존재하던 말던 무조건 oauth_company_{companyIdLowerCase}로 생성한다.
        var indexCoordinates = IndexCoordinates.of(getIndexName(companyId));
        IndexOperations indexOps = this.operations.indexOps(indexCoordinates);

        Map<String, Object> settings = this.elasticSearchSetting.get();
        indexOps.create(settings);

        Document mapping = indexOps.createMapping(this.entityInformation.getJavaType());
        indexOps.putMapping(mapping);

        return indexOps;
    }

    /**
     * namespace 가 존재하는 경우에만 alias 를 생성한다.
     *
     * @param companyId 회사 아아디
     * @param indexOps  IndexCoordinates 를 통해 얻은 IndexOperation
     */
    private void createAlias(String companyId, IndexOperations indexOps) {
        if (this.elasticSearchIndexAlias.hasNamespace()) {

            var aliasActions = new AliasActions();
            aliasActions.add(
                    new AliasAction.Add(
                            AliasActionParameters.builder()
                                    .withIndices(indexOps.getIndexCoordinates().getIndexName())
                                    .withAliases(this.getIndexNameWithAlias(companyId))
                                    .build()
                    )
            );
            indexOps.alias(aliasActions);
        }
    }


    /**
     * @return model class 에서 indexName 의 값을 가져온 뒤, 마지막 문자 *을 지우고 리턴한다.
     */
    private String getIndexNameFromEntityWithoutAsterisk() {
        String indexName = this.operations.getIndexCoordinatesFor(this.entityClass).getIndexName();

        // indexName 의 마지막 값 *를 지우고 리턴
        return StringUtils.chop(indexName);
    }

    /**
     * oauth_company 와 같이 인덱스 이름이 겹치지 않는 인덱스에서만 사용이 가능하다.
     *
     * @return model class 에서 indexName 의 * 를 제거하고 namespace 와 * 붙인 alias 를 리턴한다.
     */
    private String getIndexNameWithAliasAndAsterisk() {
        String indexName = this.getIndexNameFromEntityWithoutAsterisk();

        // alias 규칙은 indexName_{namespace}_*
        return MessageFormat.format("{0}{1}{2}", indexName, this.elasticSearchIndexAlias.getNamespace(), "*");
    }


    /**
     * @param companyId 회사 아이디
     * @return model class 에서 indexName 의 * 를 제거하고 companyId를 붙인 정확한 인덱스 이름을 리턴한다.
     */
    private String getIndexName(String companyId) {
        String indexName = this.getIndexNameFromEntityWithoutAsterisk();

        // 파라미터로 넘어온 companyId의 소문자를 붙여서 리턴한다.
        return MessageFormat.format("{0}{1}", indexName, companyId.toLowerCase());
    }


    /**
     * properties 파일에 namespace 가 설정된 경우 indexName_{namespace}_{companyIdLowerCase} 규칙으로 생성한다.
     * 없는 경우 indexName_{companyIdLowerCase}만 리턴된다.
     *
     * @param companyId 회사 아이디
     * @return model class 에서 indexName 의 * 를 제거하고 namespace 와 companyId를 붙인 alias 를 리턴한다.
     * <p>
     */
    private String getIndexNameWithAlias(String companyId) {
        String indexName = this.getIndexNameFromEntityWithoutAsterisk();

        // alias 규칙은 indexName_{namespace}_{companyIdLowerCase} 이다.
        return MessageFormat.format("{0}{1}{2}", indexName, this.elasticSearchIndexAlias.getNamespace(), companyId.toLowerCase());
    }


    /**
     * findAll 동작에서 after search 를 넣어주기 위한 함수
     *
     * @param query         findAll query
     * @param searchHitList after search 정보
     */
    private void addSearchAfterToQuery(NativeSearchQuery query, List<SearchHit<T>> searchHitList) {

        if (!searchHitList.isEmpty()) {
            List<Object> sortValues = searchHitList.get(searchHitList.size() - 1).getSortValues();

            if (!sortValues.isEmpty()) {
                query.setSearchAfter(sortValues);
            }
        }
    }


    protected ID extractIdFromBean(T entity) {
        return this.entityInformation.getId(entity);
    }


    private void deleteAllById(String companyId, Iterable<? extends ID> ids) {
        var indexCoordinates = this.getIndexCoordinates(companyId, false);

        var idsQueryBuilder = QueryBuilders.idsQuery();

        for (ID id : ids) {
            idsQueryBuilder.addIds(this.stringIdRepresentation(id));
        }

        if (!idsQueryBuilder.ids().isEmpty()) {
            operations.delete(new NativeSearchQueryBuilder().withQuery(idsQueryBuilder).build(), this.entityClass, indexCoordinates);
            this.doRefresh(indexCoordinates);
        }
    }

    private String stringIdRepresentation(@Nullable ID id) {
        return this.operations.stringIdRepresentation(id);
    }

    private void doRefresh(IndexCoordinates indexCoordinates) {

        this.operations.indexOps(indexCoordinates).refresh();

    }
}
