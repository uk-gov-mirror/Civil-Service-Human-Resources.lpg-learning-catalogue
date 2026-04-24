package uk.gov.cslearning.catalogue.api;

import org.glassfish.jersey.servlet.WebConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.repository.CourseRepository;
import uk.gov.cslearning.catalogue.service.AuthoritiesService;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.RegistryService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(SearchController.class)
@WithMockUser(username = "user")
@EnableSpringDataWebSupport
@ContextConfiguration(classes = {WebConfig.class, SearchController.class})
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private RegistryService registryService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private AuthoritiesService authoritiesService;

    @Test
    @WithMockUser(username = "user", authorities = {"ORGANISATION_AUTHOR"})
    public void shouldReturnSearchPageOfCoursesForOrganisationAuthor() throws Exception {
        PageImpl<Course> page = new PageImpl<>(Collections.emptyList());
        SearchResults searchResults = new SearchResults(page, new PageParameters().getPageRequest());
        CivilServant civilServant = mock(CivilServant.class);

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);
        when(courseRepository.search(any(Pageable.class), any(CourseSearchParameters.class), any(OwnerParameters.class)))
                .thenReturn(searchResults);

        mockMvc.perform(
                        get("/search/management/courses")
                                .param("query", "test")
                                .with(csrf()))
                .andExpect(status().isOk());

        verify(civilServant).getOrganisationalUnitCode();
    }

    @Test
    @WithMockUser(username = "user", authorities = {"KPMG_SUPPLIER_AUTHOR"})
    public void shouldReturnSearchPageOfCoursesForSupplierAuthor() throws Exception {
        PageImpl<Course> page = new PageImpl<>(Collections.emptyList());
        SearchResults searchResults = new SearchResults(page, new PageParameters().getPageRequest());
        CivilServant civilServant = mock(CivilServant.class);

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);
        when(courseRepository.search(any(Pageable.class), any(CourseSearchParameters.class), any(OwnerParameters.class)))
                .thenReturn(searchResults);

        mockMvc.perform(
                        get("/search/management/courses")
                                .param("query", "test")
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"CSL_AUTHOR"})
    public void shouldReturnSearchPageOfCoursesForCslAuthor() throws Exception {
        PageImpl<Course> page = new PageImpl<>(Collections.emptyList());
        SearchResults searchResults = new SearchResults(page, new PageParameters().getPageRequest());
        CivilServant civilServant = mock(CivilServant.class);

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);
        when(courseRepository.search(any(Pageable.class), any(CourseSearchParameters.class)))
                .thenReturn(searchResults);

        mockMvc.perform(
                        get("/search/management/courses")
                                .param("query", "test")
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"INVALID_ROLE"})
    public void shouldReturnSearchPageOfCoursesForInvalidRoles() throws Exception {
        mockMvc.perform(
                        get("/search/management/courses")
                                .param("query", "test")
                                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
