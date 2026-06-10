package uk.gov.cslearning.catalogue.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.jersey.servlet.WebConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.CivilServant.OrganisationalUnit;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.domain.Visibility;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.RegistryService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(CourseController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, CourseController.class})
@EnableSpringDataWebSupport
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private CourseService courseService;

    @MockBean
    private RegistryService registryService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void shouldCreateCourseAndRedirectToNewResource() throws Exception {
        final String newId = "newId";

        Course course = createCourse();

        when(courseService.createCourse(any(), any()))
                .thenAnswer((Answer<Course>) invocation -> {
                    course.setId(newId);
                    return course;
                });

        mockMvc.perform(
                        post("/courses").with(csrf())
                                .content(objectMapper.writeValueAsString(course))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", "http://localhost/courses/" + newId));
    }

    @Test
    public void shouldDefaultToShowingAllPublicCourses() throws Exception {
        Course course = new Course();

        when(courseRepository.findAllByStatusIn(eq(Collections.singletonList(Status.PUBLISHED)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(course)));

        mockMvc.perform(
                        get("/courses/")
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].id", equalTo(course.getId())));
    }

    @Test
    public void shouldFindMultipleStatuses() throws Exception {
        Course course = new Course();

        when(courseRepository.findAllByStatusIn(eq(Arrays.asList(Status.DRAFT, Status.PUBLISHED, Status.ARCHIVED)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(course)));

        mockMvc.perform(
                        get("/courses/")
                                .param("status", "Draft", "Published", "Archived")
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].id", equalTo(course.getId())));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ORGANISATION_AUTHOR"})
    public void shouldListForOrganisation() throws Exception {
        Course course = new Course();

        CivilServant civilServant = new CivilServant();
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        String code = "code";
        organisationalUnit.setCode(code);
        civilServant.setOrganisationalUnit(organisationalUnit);

        when(registryService.getCurrentCivilServant())
                .thenReturn(civilServant);
        when(courseService.findCoursesByOrganisationalUnit(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(course)));

        mockMvc.perform(
                        get("/courses/management")
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"KPMG_SUPPLIER_AUTHOR"})
    public void shouldListForSupplier() throws Exception {
        Course course = new Course();

        CivilServant civilServant = new CivilServant();
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        String code = "code";
        organisationalUnit.setCode(code);
        civilServant.setOrganisationalUnit(organisationalUnit);

        when(registryService.getCurrentCivilServant())
                .thenReturn(civilServant);
        when(courseService.findCoursesBySupplier(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(course)));

        mockMvc.perform(
                        get("/courses/management")
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"CSL_AUTHOR", "LEARNING_MANAGER"})
    public void shouldListForCslAuthorOrLearningManager() throws Exception {
        Course course = new Course();

        when(courseService.findAllCourses(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(course)));

        mockMvc.perform(
                        get("/courses/management")
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"INVALID_ROLE"})
    public void shouldReturnForbiddenForCslAuthor() throws Exception {
        mockMvc.perform(
                        get("/courses/management")
                                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFoundForUnknownCourse() throws Exception {
        when(courseService.findById("1"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/courses/abc")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturnCourse() throws Exception {

        Course course = createCourse();

        when(courseService.findById("1", false))
                .thenReturn(Optional.of(course));

        mockMvc.perform(
                        get("/courses/1")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("title")));
    }

    private Course createCourse() {
        return new Course("title", "shortDescription", "description",
                Visibility.PUBLIC);
    }
}
