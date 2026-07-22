package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.CourseEntity;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.service.CourseService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseLearningTagControllerTest extends MySQLIntegrationTestBase {

    @Autowired
    private ICourseRepository courseRepository;

    @MockBean
    private CourseService courseService;

    @Test
    @Transactional
    public void testAddLearningTagToCourse() throws Exception {
        CourseEntity course = new CourseEntity("course-uid", "Test Course");
        course = courseRepository.save(course);
        String courseUid = course.getUid();
        Long courseId = course.getId();

        mvc.perform(post("/courses/" + courseUid + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"PM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.uid").value("course-uid"))
                .andExpect(jsonPath("$.title").value("Test Course"))
                .andExpect(jsonPath("$.learningTag.id").value(1))
                .andExpect(jsonPath("$.learningTag.name").value("Project management"));
    }

    @Test
    @Transactional
    public void testRemoveLearningTagFromCourse() throws Exception {
        CourseEntity course = new CourseEntity("course-uid-2", "Test Course 2");
        course = courseRepository.save(course);
        String courseUid = course.getUid();

        // Add tag first
        mvc.perform(post("/courses/" + courseUid + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"TECH\"}"))
                .andExpect(status().isCreated());

        // Remove tag
        mvc.perform(delete("/courses/" + courseUid + "/learning-tags/TECH"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    public void testAddLearningTagToCourseImportingFromCourseService() throws Exception {
        String courseUid = "imported-course-uid";
        String courseTitle = "Imported Course Title";
        Course course = new Course();
        course.setId(courseUid);
        course.setTitle(courseTitle);

        when(courseService.getCourseById(courseUid)).thenReturn(course);

        mvc.perform(post("/courses/" + courseUid + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"PM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uid").value(courseUid))
                .andExpect(jsonPath("$.title").value(courseTitle))
                .andExpect(jsonPath("$.learningTag.id").value(1));
    }

    @Test
    @Transactional
    public void testAddTagToNonExistentCourse() throws Exception {
        String nonExistentUid = "non-existent-uid";
        when(courseService.getCourseById(nonExistentUid)).thenThrow(new IllegalStateException("Not found"));

        mvc.perform(post("/courses/" + nonExistentUid + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"PM\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void testAddNonExistentTagToCourse() throws Exception {
        CourseEntity course = new CourseEntity("course-uid-3", "Test Course 3");
        course = courseRepository.save(course);
        String courseUid = course.getUid();

        mvc.perform(post("/courses/" + courseUid + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"NON_EXISTENT\"}"))
                .andExpect(status().isNotFound());
    }
}
