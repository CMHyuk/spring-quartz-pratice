/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : ElasticSearchNormalizerConfig
 * author: hyunwoo.song
 * description: index 정보에 settings 를 설정하여 normalizer 를 추가하기 위한 리소스 로더 역할
 */

package com.example.quartz.elasticsearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ElasticSearchSetting {

    private final ObjectMapper objectMapper;
    private Map<String, Object> settings;

    @PostConstruct
    public void initialize() {
        this.settings = loadFromResource();
    }


    public Map<String, Object> get() {
        return this.settings;
    }

    private Map<String, Object> loadFromResource() {

        try {
            var classPathResource = new ClassPathResource("lower_case_normalizer_setting.json");

            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            var jsonText = new String(bytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(jsonText, Map.class);

        } catch (IOException e) {
            log.error("load failed : lower_case_normalizer_setting.json ( {} )", e.getMessage());
        }

        return Collections.emptyMap();
    }

}
