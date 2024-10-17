package com.example.quartz;

import com.example.quartz.elasticsearch.base.CustomAwareRepositoryImpl;
import com.example.quartz.elasticsearch.config.CustomAwareRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
// ElasticSearchJpaRepository 대신 커스텀 클래스를 지정
@EnableElasticsearchRepositories(repositoryBaseClass = CustomAwareRepositoryImpl.class, repositoryFactoryBeanClass = CustomAwareRepositoryFactoryBean.class)
public class QuartzApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzApplication.class, args);
    }

}
