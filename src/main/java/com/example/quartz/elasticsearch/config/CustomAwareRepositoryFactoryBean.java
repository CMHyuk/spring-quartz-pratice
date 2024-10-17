/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : CustomAwareRepositoryFactoryBean
 * author: hyunwoo.song
 * description: @EnableElasticsearchRepositories 에서 기본적으로 호출하는 ElasticsearchRepositoryFactoryBean 를 상속받아 커스텀한 RepositoryFactory 를 생성한다.
 * 단순하게 ElasticSearchSetting, ElasticSearchIndexAlias 컴포넌트를 주입시키기 위한 동작을 한다.
 */

package com.example.quartz.elasticsearch.config;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

@SuppressWarnings("java:S119") // ID를 다른 이름으로 변경하라는 워닝을 무시
public class CustomAwareRepositoryFactoryBean<T extends ElasticsearchRepository<S, ID>, S, ID extends Serializable> extends ElasticsearchRepositoryFactoryBean<T, S, ID> {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticSearchSetting elasticSearchSetting;
    private final ElasticSearchIndexAlias elasticSearchIndexAlias;


    public CustomAwareRepositoryFactoryBean(Class<? extends T> repositoryInterface, ElasticsearchOperations elasticsearchOperations, ElasticSearchSetting elasticSearchSetting, ElasticSearchIndexAlias elasticSearchIndexAlias) {
        super(repositoryInterface);
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticSearchSetting = elasticSearchSetting;
        this.elasticSearchIndexAlias = elasticSearchIndexAlias;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new CustomAwareRepositoryFactory(elasticsearchOperations, elasticSearchSetting, elasticSearchIndexAlias);
    }


}
