package uk.gov.cslearning.catalogue.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.cslearning.catalogue.config.DisableBlobStorage;
import uk.gov.cslearning.catalogue.config.IntegrationTestConfig;
import uk.gov.cslearning.catalogue.util.DataService;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ActiveProfiles("integration-test")
@RunWith(SpringRunner.class)
@Import({IntegrationTestConfig.class, DisableBlobStorage.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected DataService dataService;

    @Autowired
    protected WebApplicationContext context;

    static boolean runIntegrationTests = Boolean.parseBoolean(System.getenv("RUN_INTEGRATION_TESTS"));
    static boolean useLocalElastic = Boolean.parseBoolean(System.getenv("INTEGRATION_TEST_USE_LOCAL_ELASTIC"));

    @BeforeClass
    public static void beforeClass() {
        Assume.assumeTrue("RUN_INTEGRATION_TESTS is false, skipping integration tests", runIntegrationTests);
    }

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(result -> {
                    System.out.println("API RESPONSE");
                    String json = result.getResponse().getContentAsString();
                    System.out.println(json);
                })
                .build();
    }

    /**
     * TestContainers takes a very long time to spin up which is causing timeouts in the integration tests. Assuming
     * this will be fixed if we upgraded to Java 17 so lets comment this out until then.
     */

    protected static String elasticImage = "docker.elastic.co/elasticsearch/elasticsearch:8.4.2";

    static ElasticsearchContainer ELASTICSEARCH_CONTAINER;
    static {
        if (runIntegrationTests) {
            if (!useLocalElastic) {
                log.info("INTEGRATION_TEST_USE_LOCAL_ELASTIC setting is false. Creating an elasticsearch container.");
                ELASTICSEARCH_CONTAINER = new ElasticsearchContainer(DockerImageName.parse(elasticImage));
                ELASTICSEARCH_CONTAINER.withEnv("discovery.type", "single-node");
                ELASTICSEARCH_CONTAINER.withEnv("cluster.name", "local");
                ELASTICSEARCH_CONTAINER.withEnv("xpack.security.enabled", "false");
                ELASTICSEARCH_CONTAINER.withExposedPorts(9200);
                ELASTICSEARCH_CONTAINER.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                        new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(9200), new ExposedPort(9200)))
                ));
                ELASTICSEARCH_CONTAINER.withStartupTimeout(Duration.of(5, ChronoUnit.MINUTES));
                ELASTICSEARCH_CONTAINER.start();
            } else {
                log.info("INTEGRATION_TEST_USE_LOCAL_ELASTIC setting is true. Using a local elasticsearch instance on port 9200.");
            }
        } else {
            log.info("RUN_INTEGRATION_TESTS setting is false. Skipping elastic container setup");
        }
    }

    @Before
    public void beforeAll() {
        dataService.deleteBulkCourses();
        dataService.loadBulkCourses();
    }

//    @After
//    public void afterAll() {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> dataService.deleteBulkCourses()));
//    }

}
