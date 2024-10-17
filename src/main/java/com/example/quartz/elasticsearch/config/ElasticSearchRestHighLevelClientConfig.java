/*
 * Copyright (c) 2023 SOFTCAMP Co.,LTD. - All Rights Reserved
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium is strictly prohibited.
 * class : ElasticSearchConfig
 * author: hyunwoo.song
 * description: RestHighLevelClient를 설정하는 클래스이다.
 */

package com.example.quartz.elasticsearch.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.time.Duration;

@Slf4j
@Configuration
public class ElasticSearchRestHighLevelClientConfig {

    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.expected-http-response-time-millis:50}")
    private int expectedHttpResponseMilliSeconds;    // http 호출의 평균 응답 시간 예상 값(밀리초)

    @Value("${elasticsearch.expected-throughput-in-request-per-seconds:500}")
    private int expectedThroughputInRequestPerSeconds;    // 초당 요청 처리량 예상 값

    @Value("${elasticsearch.socket-timeout-seconds:60}")
    private int socketTimeoutSeconds;

    @Value("${elasticsearch.connection-timeout-seconds:30}")
    private int connectionTimeoutSeconds;

    @Value("${elasticsearch.connection-request-timeout-seconds:30}")
    private int connectionRequestTimeoutSeconds;

    @Value("${elasticsearch.keep-alive-strategy-time-minutes:5}")
    private int keepAliveStrategyTimeMinutes;


    @Bean
    public RestHighLevelClient restHighLevelClient() {
        log.info("ElasticSearch target host = {}", host);
        int maxConnTotal = determineMaxConnectionTotal();
        var restClientBuilder = RestClient.builder(new HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(
                        httpClientBuilder ->
                                httpClientBuilder.setConnectionReuseStrategy((httpResponse, httpContext) -> true)
                                        .setKeepAliveStrategy((httpResponse, httpContext) -> Duration.ofMinutes(this.keepAliveStrategyTimeMinutes).toMillis())
                                        .setMaxConnTotal(maxConnTotal)
                                        .setMaxConnPerRoute(maxConnTotal)
                )
                .setRequestConfigCallback(
                        requestConfig ->
                                requestConfig.setConnectTimeout((int) Duration.ofSeconds(this.connectionTimeoutSeconds).toMillis())
                                        .setSocketTimeout((int) Duration.ofSeconds(this.socketTimeoutSeconds).toMillis())
                                        .setConnectionRequestTimeout((int) Duration.ofSeconds(this.connectionRequestTimeoutSeconds).toMillis())
                );

        return new RestHighLevelClient(restClientBuilder);
    }

    private int determineMaxConnectionTotal() {
        // RestClient의 DEFAULT_MAX_CONN_TOTAL 값은 30이다. 이 값을 기본으로 사용하는 경우 리소스를 제한적으로 사용하는 케이스가 되버리므로 성능이 제대로 나오지 않는다.
        // 필요 시 application.yml 에 설정할 수 있도록 한다.

        // CPU 코어당 커넥션(X)을 사용할 개수를 계산한다.
        // 기본 값으로 초당 요청 (Q)이 500개, 평균 응답 시간(R)이 50 ms 라고 한다면
        // X = Q * R / 1,000 으로 계산하여 25개의 값을 사용한다.
        int maxConnTotalPerCPU = (this.expectedThroughputInRequestPerSeconds * this.expectedHttpResponseMilliSeconds) / 1_000;

        // 최소 값인 경우 CPU당 10개 커넥션으로 설정
        if (maxConnTotalPerCPU <= 0) {
            maxConnTotalPerCPU = 10;
        }
        // CPU 개수랑 곱한다.
        int maxConnTotal = Runtime.getRuntime().availableProcessors() * maxConnTotalPerCPU;

        // Tomcat 8버전 부터는 기본 커넥터를 NIO 를 사용하며 (7버전 까지는 BIO)
        // Tomcat의 기본 maxConnection은 10,000, 기본 maxThreads는 200이 기본값이다. ( Embedded Tomcat은 기본 maxConnections이 8,192 개라고 한다.. )
        // 따라서 전체 커넥션이 10,000보다 큰 경우는 8,000 개로 줄여서 넘기도록 한다.
        return maxConnTotal < 10_000 ? maxConnTotal : 8_000;

        // Tomcat에서 설정 가능하다고 적용되는 것은 아니며, 리눅스의 ulimit 값을 참고해야 한다.
        // 자바에서 소켓통신은 ulimit 옵션의 open file 옵션을 따라가며, OS 튜닝을 별도로 하지 않았다면 ulimit은 일반적으로 soft limit이 hard limit보다 작게 설정되어 있다.

        // 그러나 JDK 내부적으로 hard limit 값을 soft limit 값으로 업데이트 하여 사용한다.
        // 단, 자바의 MaxFDLimit이 true인 경우에만 업데이트가 된다. ( java -XX:+PrintFlagsFinal -version | grep MaxFDLimit으로 확인할 수 있다.)

        // 현재 Amazon Corretto에 설정된 JDK에는 true가 기본값이며 1,048,576 개로 설정되어 있다.
        // 만약 별도로 OS에서 직접 실행한다고 하면 ulimit -Sn 과 ulimit -Hn을 확인후 적절하게 튜닝이 필요하다.
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(restHighLevelClient());
    }



}
