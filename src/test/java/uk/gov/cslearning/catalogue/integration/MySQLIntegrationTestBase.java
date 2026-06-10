package uk.gov.cslearning.catalogue.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cslearning.catalogue.config.DisableBlobStorage;
import uk.gov.cslearning.catalogue.config.IntegrationTestConfig;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.repository.elastic.MediaRepository;
import uk.gov.cslearning.catalogue.util.DataService;

@ActiveProfiles("mysql")
@RunWith(SpringRunner.class)
@Import({IntegrationTestConfig.class, DisableBlobStorage.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class MySQLIntegrationTestBase {

    /*
    Mock out the Elasticsearch repositories to prevent the app connecting to ES.
    The ES tests should be moved to their own package at some point, maybe after the Java upgrade / maven migration
     */

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private MediaRepository mediaRepository;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected DataService dataService;

    @Autowired
    protected WebApplicationContext context;
}
