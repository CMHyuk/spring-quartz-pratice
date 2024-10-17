/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : ElasticSearchAliasConfig
 * author: hyunwoo.song
 * description: 환경변수에서 쿠버네티스 네임스페이스 값을 얻어서 조합된 문자열을 리턴하기 위한 컴포넌트이다.
 */

package com.example.quartz.elasticsearch.config;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;


@Component
public class ElasticSearchIndexAlias {

    @Value("${kubernetes.cluster.namespace:}")
    String namespace;

    /**
     * @return properties 에 namespace 가 설정되어 있으면 namespace_{kubernetes.cluster.namespace}_ 의 규칙으로 만들어 리턴한다.
     */
    public String getNamespace() {

        if (StringUtils.isEmpty(namespace)) {
            return "";
        }
        return MessageFormat.format("namespace_{0}_", namespace);
    }

    public boolean hasNamespace() {
        return StringUtils.isNotEmpty(this.namespace);
    }

}
