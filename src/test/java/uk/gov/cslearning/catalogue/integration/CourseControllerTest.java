package uk.gov.cslearning.catalogue.integration;

import org.elasticsearch.common.collect.List;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.domain.module.ELearningModule;
import uk.gov.cslearning.catalogue.service.RegistryService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseControllerTest extends IntegrationTestBase {

    @MockBean
    private RegistryService mockRegistryService;

    @Before
    public void before() {
        // Any endpoint annotated with @PreAuthorize will call CsrsService to get the current civil servant.
        // We should mock this call out with wiremock but until then let's just mock the method out
        when(mockRegistryService.getCurrentCivilServant()).thenReturn(new CivilServant());
    }


    @Test
    @Order(1)
    public void testGetCourses() throws Exception {

        mvc.perform(get("/courses")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].title").value("Required course 1"))
                .andExpect(jsonPath("$.results[0].shortDescription").value("Required course 1 short description"))
                .andExpect(jsonPath("$.results[0].description").value("Required course 1 long description"))
                .andExpect(jsonPath("$.results[0].status").value("Published"))
                .andExpect(jsonPath("$.results[1].title").value("Required course 2"))
                .andExpect(jsonPath("$.results[1].shortDescription").value("Required course 2 short description"))
                .andExpect(jsonPath("$.results[1].description").value("Required course 2 long description"))
                .andExpect(jsonPath("$.results[1].status").value("Published"));
    }

    @Test
    @Order(2)
    public void testGetCoursesWithPagination() throws Exception {

        mvc.perform(get("/courses?size=1&page=0")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults").value(13))
                .andExpect(jsonPath("$.results[0].title").value("Required course 1"))
                .andExpect(jsonPath("$.results[0].shortDescription").value("Required course 1 short description"))
                .andExpect(jsonPath("$.results[0].description").value("Required course 1 long description"))
                .andExpect(jsonPath("$.results[0].status").value("Published"));

        mvc.perform(get("/courses?size=1&page=1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].title").value("Required course 2"))
                .andExpect(jsonPath("$.results[0].shortDescription").value("Required course 2 short description"))
                .andExpect(jsonPath("$.results[0].description").value("Required course 2 long description"))
                .andExpect(jsonPath("$.results[0].status").value("Published"));

    }

    @Test
    @Order(2)
    public void testGetMandatoryLearning() throws Exception {

        mvc.perform(get("/courses?mandatory=true&days=7&size=1000000000")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.HMRC[0].title").value("Required course 2"))
                .andExpect(jsonPath("$.HMRC[0].shortDescription").value("Required course 2 short description"))
                .andExpect(jsonPath("$.HMRC[0].description").value("Required course 2 long description"))
                .andExpect(jsonPath("$.HMRC[0].status").value("Published"));

    }

    @Test
    @Order(3)
    @WithMockUser(value = "spring", authorities = {"CSL_AUTHOR"})
    public void testCreateModule() throws Exception {

        Course tempCourse = dataService.createCourse("CreateModule");
        dataService.getRepository().save(tempCourse);

        String json = new JSONObject()
                .put("url", "http://url.com")
                .put("title", "Link module")
                .put("description", "A link module")
                .put("duration",  100)
                .put("cost",  0)
                .put("optional",  false)
                .put("associatedLearning",  false)
                .put("type", "link")
                .toString();

        mvc.perform(post(String.format("/courses/%s/modules", tempCourse.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        mvc.perform(get(String.format("/courses/%s", tempCourse.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[0].title").value("Link module"))
                .andExpect(jsonPath("$.modules[0].description").value("A link module"))
                .andExpect(jsonPath("$.modules[0].cost").value(0))
                .andExpect(jsonPath("$.modules[0].duration").value(100))
                .andExpect(jsonPath("$.modules[0].moduleType").value("link"))
                .andExpect(jsonPath("$.modules[0].type").value("link"))
                .andExpect(jsonPath("$.modules[0].optional").value(false));

        dataService.getRepository().delete(tempCourse);

    }

    @Test
    @Order(4)
    @WithMockUser(value = "spring", authorities = {"LEARNING_UNARCHIVE"})
    public void testUnarchiveCourse() throws Exception {
        Course tempCourse = dataService.createCourse("archiveCourse");
        tempCourse.setStatus(Status.ARCHIVED);
        dataService.getRepository().save(tempCourse);

        Course update = dataService.getRepository().findById(tempCourse.getId()).get();
        update.setStatus(Status.DRAFT);
        mvc.perform(put(String.format("/courses/%s", tempCourse.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isOk());
        assertEquals(Status.DRAFT, dataService.getRepository().findById(tempCourse.getId()).get().getStatus());
    }

    @Test
    @Order(5)
    @WithMockUser(value = "spring", authorities = {"LEARNING_PUBLISH"})
    public void testUnarchiveCourseIncorrectPermission() throws Exception {
        Course tempCourse = dataService.createCourse("archiveCourse");
        tempCourse.setStatus(Status.ARCHIVED);
        dataService.getRepository().save(tempCourse);

        Course update = dataService.getRepository().findById(tempCourse.getId()).get();
        update.setStatus(Status.DRAFT);
        mvc.perform(put(String.format("/courses/%s", tempCourse.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @WithMockUser(value = "spring", authorities = {"LEARNING_EDIT"})
    public void testOrderModules() throws Exception {
        Course tempCourse = dataService.createCourse("orderModules");
        ELearningModule m1 = dataService.createELearningModule();
        m1.setId("FIRST");
        ELearningModule m2 = dataService.createELearningModule();
        m2.setId("SECOND");
        tempCourse.setModules(List.of(m1, m2));
        dataService.getRepository().save(tempCourse);

        mvc.perform(put(String.format("/courses/%s/modules", tempCourse.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"startPage\":\"http://startPage\",\"url\":\"http://url.com\",\"id\":\"SECOND\",\"title\":\"ELearning module\",\"description\":\"An ELearning module\",\"duration\":100,\"cost\":0.0,\"optional\":false,\"status\":null,\"associatedLearning\":false,\"createdTimestamp\":null,\"updatedTimestamp\":null,\"mediaId\":null,\"moduleType\":\"elearning\",\"type\":\"elearning\"},{\"startPage\":\"http://startPage\",\"url\":\"http://url.com\",\"id\":\"FIRST\",\"title\":\"ELearning module\",\"description\":\"An ELearning module\",\"duration\":100,\"cost\":0.0,\"optional\":false,\"status\":null,\"associatedLearning\":false,\"createdTimestamp\":null,\"updatedTimestamp\":null,\"mediaId\":null,\"moduleType\":\"elearning\",\"type\":\"elearning\"}]"))
                .andExpect(status().isOk());
        Course result = dataService.getRepository().findById(tempCourse.getId()).get();
        assertEquals("SECOND", result.getModules().get(0).getId());
        assertEquals("FIRST", result.getModules().get(1).getId());
    }

    @Test
    @Order(7)
    @WithMockUser(value = "spring", authorities = {"CSL_AUTHOR"})
    public void testUpdatingCourseToStatusPublishedSetsHasBeenPublishedToTrue() throws Exception {
        String courseId = "test-course-01";

        Course course = new Course();
        course.setId(courseId);
        course.setTitle("My course");
        course.setStatus(Status.DRAFT);
        course.setHasBeenPublished(false);

        dataService.getRepository().save(course);

        String json = new JSONObject()
                .put("id", courseId)
                .put("title", "My course")
                .put("status", "Published")
                .toString();

        mvc.perform(put(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());


        mvc.perform(get(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Published"))
                .andExpect(jsonPath("$.hasBeenPublished").value(true));
    }

    @Test
    @Order(8)
    @WithMockUser(value = "spring", authorities = {"CSL_AUTHOR"})
    public void testUpdatingCourseToStatusDraftDoesNotChangeHasBeenPublishedValueFromTrue() throws Exception {
        String courseId = "test-course-02";

        Course course = new Course();
        course.setId(courseId);
        course.setTitle("My course 02");
        course.setStatus(Status.PUBLISHED);
        course.setHasBeenPublished(true);

        dataService.getRepository().save(course);

        String json = new JSONObject()
                .put("id", courseId)
                .put("title", "My course")
                .put("status", "Draft")
                .toString();

        mvc.perform(put(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());


        mvc.perform(get(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Draft"))
                .andExpect(jsonPath("$.hasBeenPublished").value(true));
    }

    @Test
    @Order(9)
    @WithMockUser(value = "spring", authorities = {"CSL_AUTHOR"})
    public void testDeletingCourseRemovesDocumentIfCourseIsDraftAndNotPublished() throws Exception {
        Course course = new Course();
        course.setTitle("My course 03");
        course.setStatus(Status.DRAFT);
        course.setHasBeenPublished(false);

        dataService.getRepository().save(course);

        mvc.perform(delete(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        mvc.perform(get(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @WithMockUser(value = "spring", authorities = {"CSL_AUTHOR"})
    public void testDeletingCourseDoesNotRemovesDocumentIfCourseIsDraftAndPublishedAndThrowsConflictError() throws Exception {
        String courseId = "test-course-04";

        Course course = new Course();
        course.setId(courseId);
        course.setTitle("My course 04");
        course.setStatus(Status.DRAFT);
        course.setHasBeenPublished(true);

        dataService.getRepository().save(course);

        mvc.perform(delete(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());


        mvc.perform(get(String.format("/courses/%s", course.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
