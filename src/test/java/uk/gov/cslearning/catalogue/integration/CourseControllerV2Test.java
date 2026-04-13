package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseControllerV2Test extends IntegrationTestBase {

    @Test
    public void testGetRequiredLearningDepartmentMap() throws Exception {
        mvc.perform(get("/v2/courses/required-learning-map")
                .with(csrf()))
                .andExpect(jsonPath("$.departmentCodeMap.HMRC[0]").value("Required course 1"))
                .andExpect(jsonPath("$.departmentCodeMap.HMRC[1]").value("Required course 2"))
                .andExpect(jsonPath("$.departmentCodeMap.CO[0]").value("Required course 1"));
    }

    @Test
    public void testCourseSearchPost() throws Exception {
        mvc.perform(post("/v2/courses/search?size=1&page=0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseIds\": [\"req-course-1\", \"req-course-2\"], \"query\":  \"2\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].id").value("req-course-2"))
                .andExpect(jsonPath("$.results[0].title").value("Required course 2"));
    }

}
