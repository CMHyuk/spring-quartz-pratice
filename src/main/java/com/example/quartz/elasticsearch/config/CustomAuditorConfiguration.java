/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : CustomAuditorAware
 * author: hyunwoo.song
 * description: @EnableElasticsearchRepositories 에서 CustomAwareRepositoryFactoryBean 를 설정한 경우
 * @EnableElasticsearchAuditing 를 사용하기 위해서
 * ElasticSearchConfigurationSupport 의 SimpleElasticsearchMappingContext 를 주입받아야 한다.
 * 이후 @CreateDate 와 같은 어노테이션을 사용할 수 있다.
 */

package com.example.quartz.elasticsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;

@Configuration
@EnableElasticsearchAuditing
public class CustomAuditorConfiguration extends ElasticsearchConfigurationSupport {
}
