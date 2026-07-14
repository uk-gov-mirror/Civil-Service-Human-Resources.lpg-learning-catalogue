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
import uk.gov.cslearning.catalogue.domain.module.LinkModule;
import uk.gov.cslearning.catalogue.domain.module.Module;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.ModuleService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(ModuleController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, ModuleController.class})
@EnableSpringDataWebSupport
public class ModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private ModuleService moduleService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void shouldCreateModule() throws Exception {
        String moduleId = "module-id";
        Module module = mock(LinkModule.class);
        when(module.getId()).thenReturn(moduleId);

        String courseId = UUID.randomUUID().toString();
        String json = objectMapper.writeValueAsString(ImmutableMap.of("type", "link", "location", "http://localhost"));

        when(moduleService.save(eq(courseId), any(Module.class))).thenReturn(module);

        mockMvc.perform(
                        post(String.format("/courses/%s/modules/", courseId)).with(csrf())
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", String.format("http://localhost/courses/%s/modules/%s", courseId, moduleId)));
    }

    @Test
    public void shouldFindModule() throws Exception {
        String courseId = "course-id";
        String moduleId = "module-id";
        String url = "http://example.org";

        Module module = new LinkModule(new URL(url));

        when(moduleService.find(courseId, moduleId)).thenReturn(Optional.of(module));

        mockMvc.perform(
                        get(String.format("/courses/%s/modules/%s", courseId, moduleId)).with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", equalTo(url)));
    }

    @Test
    public void shouldReturnNotFoundIfModuleNotFound() throws Exception {
        String courseId = "course-id";
        String moduleId = "module-id";

        when(moduleService.find(courseId, moduleId)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get(String.format("/courses/%s/modules/%s", courseId, moduleId)).with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteModule() throws Exception {
        String courseId = "course-id";
        String moduleId = "module-id";

        doNothing().when(moduleService).deleteModule(courseId, moduleId);

        mockMvc.perform(
                        delete(String.format("/courses/%s/modules/%s", courseId, moduleId)).with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldUpdateModule() throws Exception {
        String courseId = "course-id";
        String moduleId = "id-123";
        String title = "old-title";
        String updatedTitle = "updated-title";
        String url = "https://www.example.org";

        Course course = new Course();
        Course updatedCourse = new Course();

        Module module = new LinkModule(new URL(url));
        module.setId(moduleId);
        module.setTitle(title);

        Module updatedModule = new LinkModule(new URL(url));
        updatedModule.setId(moduleId);
        updatedModule.setTitle(updatedTitle);

        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);

        List<Module> updatedModules = new ArrayList<>();
        updatedModules.add(updatedModule);
        updatedCourse.setModules(updatedModules);

        when(moduleService.find(any(), any())).thenReturn(Optional.of(module));
        when(moduleService.updateModule(courseId, updatedModule)).thenReturn(updatedCourse);

        mockMvc.perform(
                        put(String.format("/courses/%s/modules/%s", courseId, module.getId())).with(csrf())
                                .content(objectMapper.writeValueAsString(updatedModule))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertThat(updatedCourse.getModules().isEmpty(), is(false));
        assertThat(updatedCourse.getModules().size(), is(1));
        assertThat(updatedCourse.getModules().get(0).getTitle(), is(updatedTitle));
    }

}
