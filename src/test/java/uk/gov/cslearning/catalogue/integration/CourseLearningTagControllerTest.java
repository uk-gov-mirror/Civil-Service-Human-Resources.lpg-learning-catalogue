package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cslearning.catalogue.domain.CourseEntity;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseLearningTagControllerTest extends MySQLIntegrationTestBase {

    @Autowired
    private ICourseRepository courseRepository;

    @Test
    @Transactional
    public void testAddTagToCourse() throws Exception {
        CourseEntity course = new CourseEntity("course-uid", "Test Course");
        course = courseRepository.save(course);
        Long courseId = course.getId();

        mvc.perform(post("/courses/" + courseId + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Project management"));
    }

    @Test
    @Transactional
    public void testRemoveTagFromCourse() throws Exception {
        CourseEntity course = new CourseEntity("course-uid-2", "Test Course 2");
        course = courseRepository.save(course);
        Long courseId = course.getId();

        // Add tag first
        mvc.perform(post("/courses/" + courseId + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 2}"))
                .andExpect(status().isCreated());

        // Remove tag
        mvc.perform(delete("/courses/" + courseId + "/learning-tags/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    public void testAddTagToNonExistentCourse() throws Exception {
        mvc.perform(post("/courses/999/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void testAddNonExistentTagToCourse() throws Exception {
        CourseEntity course = new CourseEntity("course-uid-3", "Test Course 3");
        course = courseRepository.save(course);
        Long courseId = course.getId();

        mvc.perform(post("/courses/" + courseId + "/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 999}"))
                .andExpect(status().isNotFound());
    }
}
