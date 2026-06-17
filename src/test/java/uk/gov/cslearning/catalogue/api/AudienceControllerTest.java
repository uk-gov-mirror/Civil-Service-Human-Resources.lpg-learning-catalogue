package uk.gov.cslearning.catalogue.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.servlet.WebConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.Audience;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.AudienceService;
import uk.gov.cslearning.catalogue.service.CourseService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(AudienceController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, AudienceController.class})
@EnableSpringDataWebSupport
public class AudienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private CourseService courseService;
    @MockBean
    private AudienceService audienceService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void shouldCreateAudience() throws Exception {
        String audienceId = "audience-id";
        Audience audience = mock(Audience.class);
        when(audience.getId()).thenReturn(audienceId);

        String courseId = UUID.randomUUID().toString();

        when(audienceService.isPermitted(any(), any())).thenReturn(true);
        when(audienceService.setDefaults(any(), any())).thenReturn(audience);
        when(audienceService.save(any(), any())).thenReturn(new Course());

        mockMvc.perform(
                        post(String.format("/courses/%s/audiences/", courseId)).with(csrf())
                                .content(objectMapper.writeValueAsString(ImmutableMap.of("id", audienceId, "name", "Audience name")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", String.format("http://localhost/courses/%s/audiences/%s", courseId, audienceId)));
    }

    @Test
    public void shouldFindAudience() throws Exception {
        String courseId = "course-id";
        String audienceId = "audience-id";

        Audience audience = new Audience();
        audience.setId(audienceId);

        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(audienceService.find(courseId, audienceId)).thenReturn(Optional.of(audience));

        mockMvc.perform(
                        get(String.format("/courses/%s/audiences/%s", courseId, audienceId)).with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(audienceId)));
    }

    @Test
    public void shouldReturnNotFoundIfAudienceNotFound() throws Exception {
        String courseId = "course-id";
        String audienceId = "audience-id";

        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(audienceService.find(courseId, audienceId)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get(String.format("/courses/%s/audiences/%s", courseId, audienceId)).with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundIfCourseNotFoundWhenFindingAudience() throws Exception {
        String courseId = "course-id";
        String audienceId = "audience-id";

        when(audienceService.isPermitted(any(), any())).thenReturn(true);
        Audience audience = new Audience();
        when(audienceService.setDefaults(any(), any())).thenReturn(audience);

        doThrow(new ResourceNotFoundException()).when(audienceService).save(courseId, audience);

        mockMvc.perform(
                        get(String.format("/courses/%s/audiences/%s", courseId, audienceId)).with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteAudience() throws Exception {
        Course course = new Course();
        Audience audience = new Audience();
        Set<Audience> audiences = new HashSet<>();
        audiences.add(audience);
        course.setAudiences(audiences);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(audienceService.find(course, audience.getId())).thenReturn(Optional.of(audience));
        when(audienceService.isPermitted(any(), any())).thenReturn(true);
        when(courseRepository.save(course)).thenReturn(course);

        assertThat(course.getAudiences().isEmpty(), is(not(true)));

        mockMvc.perform(
                        delete(String.format("/courses/%s/audiences/%s", course.getId(), audience.getId())).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(course.getAudiences().isEmpty(), is(true));
    }

}
