package uk.gov.cslearning.catalogue.config;

import lombok.SneakyThrows;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.collect.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.http.HttpHeaders;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.domain.module.EventStatus;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableElasticsearchRepositories(basePackages = "uk.gov.cslearning.catalogue.repository.elastic")
public class ElasticRestClientConfig extends AbstractElasticsearchConfiguration {

    @Value("${elasticsearch.protocol}")
    private String protocol;

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private Integer port;

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.readTimeout}")
    private int readTimeout;


    @Override
    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticsearchClient() {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, port, protocol));
        restClientBuilder
                .setHttpClientConfigCallback(builder -> builder.setDefaultCredentialsProvider(credentialsProvider))
                .setDefaultHeaders(compatibilityHeaders())
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setSocketTimeout(readTimeout));
        return new RestHighLevelClient(restClientBuilder);

    }

    private Header[] compatibilityHeaders() {
        return new Header[]{
                new BasicHeader(HttpHeaders.ACCEPT, "application/vnd.elasticsearch+json;compatible-with=7"),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.elasticsearch+json;compatible-with=7")
        };
    }


    @Bean
    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(List.of(
                new ZonedDateTimeToStringConverter(),
                new StringToZonedDateTimeConverter(),
                new URLToStringConverter(),
                new StringToURLConverter(),
                new StatusToStringConverter(),
                new StringtoStatusConverter(),
                new EventStatusToStringConverter(),
                new StringtoEventStatusConverter()));
    }

    @WritingConverter
    public class ZonedDateTimeToStringConverter implements Converter<LocalDateTime, String> {

        @Override
        public String convert(LocalDateTime source) {
            return source.format(DateTimeFormatter.ISO_DATE_TIME);
        }
    }

    @ReadingConverter
    public class StringToZonedDateTimeConverter implements Converter<String, LocalDateTime> {

        @Override
        public LocalDateTime convert(@NotNull String source) {
            return LocalDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME);
        }
    }

    @WritingConverter
    public class URLToStringConverter implements Converter<URL, String> {

        @Override
        public String convert(URL source) {
            return source.toString();
        }
    }

    @ReadingConverter
    public class StringToURLConverter implements Converter<String, URL> {

        @SneakyThrows
        @Override
        public URL convert(@NotNull String source) {
            return new URL(source);
        }
    }

    @WritingConverter
    public class StatusToStringConverter implements Converter<Status, String> {

        @Override
        public String convert(Status source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public class StringtoStatusConverter implements Converter<String, Status> {

        @SneakyThrows
        @Override
        public Status convert(@NotNull String source) {
            return Status.forValue(source);
        }
    }

    @WritingConverter
    public class EventStatusToStringConverter implements Converter<EventStatus, String> {

        @Override
        public String convert(EventStatus source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public class StringtoEventStatusConverter implements Converter<String, EventStatus> {

        @SneakyThrows
        @Override
        public EventStatus convert(@NotNull String source) {
            return EventStatus.forValue(source);
        }
    }

}
