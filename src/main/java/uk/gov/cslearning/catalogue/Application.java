package uk.gov.cslearning.catalogue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring application configuration and entry point.
 */
@SpringBootApplication(exclude = {ElasticsearchRepositoriesAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "uk.gov.cslearning.catalogue.repository.sql")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
