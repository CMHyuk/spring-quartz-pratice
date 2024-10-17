/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : CustomAwareRepositoryFactory
 * author: hyunwoo.song
 * description: ElasticsearchRepositoryFactoryBean 에서 생성하는 RepositoryFactory 이다.
 * CustomAwareRepositoryImpl 생성 시 ElasticSearchSetting, ElasticSearchIndexAlias 를 주입하기 위한 클래스이다.
 */

package com.example.quartz.elasticsearch.config;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;

public class CustomAwareRepositoryFactory extends ElasticsearchRepositoryFactory {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticSearchSetting elasticSearchSetting;
    private final ElasticSearchIndexAlias elasticSearchIndexAlias;

    public CustomAwareRepositoryFactory(ElasticsearchOperations elasticsearchOperations, ElasticSearchSetting elasticSearchSetting, ElasticSearchIndexAlias elasticSearchIndexAlias) {
        super(elasticsearchOperations);
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticSearchSetting = elasticSearchSetting;
        this.elasticSearchIndexAlias = elasticSearchIndexAlias;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return this.getTargetRepositoryViaReflection(metadata, this.getEntityInformation(metadata.getDomainType()), this.elasticsearchOperations, elasticSearchSetting, elasticSearchIndexAlias);
    }
}
