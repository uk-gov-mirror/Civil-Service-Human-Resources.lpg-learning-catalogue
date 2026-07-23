package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cslearning.catalogue.domain.CourseEntity;
import uk.gov.cslearning.catalogue.domain.CourseLearningTagEntity;
import uk.gov.cslearning.catalogue.domain.LearningTag;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseLearningTagControllerTest extends MySQLIntegrationTestBase {

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ILearningTagRepository learningTagRepository;

    @Autowired
    private ICourseTagRepository courseTagRepository;

    @Test
    @Transactional
    public void testRemoveLearningTagFromCourse() throws Exception {
        CourseEntity course = new CourseEntity("course-uid-2", "Test Course 2");
        course = courseRepository.save(course);
        String courseUid = course.getUid();

        LearningTag tag = learningTagRepository.findByCode("TECH")
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        courseTagRepository.save(new CourseLearningTagEntity(tag, course));

        // Remove tag
        mvc.perform(delete("/courses/" + courseUid + "/learning-tags/TECH"))
                .andExpect(status().isNoContent());
    }
}
