package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.cslearning.catalogue.domain.Course;

import java.util.Arrays;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CourseControllerV2Test extends IntegrationTestBase {

    @Test
    public void testGetRequiredLearningDepartmentMap() throws Exception {
        mvc.perform(get("/v2/courses/required-learning-map")
                        .with(csrf()))
                .andExpect(jsonPath("$.departmentCodeMap.HMRC[0]").value("req-course-1"))
                .andExpect(jsonPath("$.departmentCodeMap.HMRC[1]").value("req-course-2"))
                .andExpect(jsonPath("$.departmentCodeMap.CO[0]").value("req-course-1"));
    }

    @Test
    public void testGetCourseIdAudienceAttributeMap() throws Exception {
        String expectedJson = "{\n" +
                "    \"areasOfWork\": {\n" +
                "        \"Analysis\": [\n" +
                "            \"elearning-course\",\n" +
                "            \"Blended course 1\"\n" +
                "        ],\n" +
                "        \"Finance\": [\n" +
                "            \"Private course 1\",\n" +
                "            \"Blended course 2\"\n" +
                "        ],\n" +
                "        \"DDaT\": [\n" +
                "            \"Learning 1\",\n" +
                "            \"Blended course 3\"\n" +
                "        ]\n" +
                "    },\n" +
                "    \"departments\": {\n" +
                "        \"MOD\": [\n" +
                "            \"Learning 1\",\n" +
                "            \"Blended course 3\"\n" +
                "        ],\n" +
                "        \"COD\": [\n" +
                "            \"Video course\",\n" +
                "            \"Learning 1\",\n" +
                "            \"Blended course 2\",\n" +
                "            \"Blended course 3\"\n" +
                "        ],\n" +
                "        \"CO\": [\n" +
                "            \"req-course-2\"\n" +
                "        ],\n" +
                "        \"DWP\": [\n" +
                "            \"Blended course 2\",\n" +
                "            \"req-course-1\"\n" +
                "        ]\n" +
                "    },\n" +
                "    \"interests\": {\n" +
                "        \"EU\": [\n" +
                "            \"File course\",\n" +
                "            \"Blended course 1\"\n" +
                "        ],\n" +
                "        \"Parliament\": [\n" +
                "            \"Blended course 1\"\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        mvc.perform(get("/v2/courses/audience-attribute-map")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testCourseSearchPost() throws Exception {
        Course ZCourse = dataService.createCourse("Z course");
        ZCourse.setId("ZCourse");

        Course ACourse = dataService.createCourse("A course");
        ACourse.setId("Acourse");

        Course bCourse = dataService.createCourse("b course");
        bCourse.setId("bcourse");

        Course otherCourse = dataService.createCourse("Other");
        otherCourse.setId("otherCourse");

        dataService.getRepository().saveAll(Arrays.asList(bCourse, ACourse, ZCourse, otherCourse));

        // Sorted
        mvc.perform(post("/v2/courses/search?size=4&page=0&sort.field=title&sort.direction=ASC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseIds\": [\"bcourse\", \"Acourse\", \"ZCourse\", \"otherCourse\"], \"query\":  \"course\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(3))
                .andExpect(jsonPath("$.results[0].id").value("Acourse"))
                .andExpect(jsonPath("$.results[0].title").value("A course"))
                .andExpect(jsonPath("$.results[1].id").value("bcourse"))
                .andExpect(jsonPath("$.results[1].title").value("b course"))
                .andExpect(jsonPath("$.results[2].id").value("ZCourse"))
                .andExpect(jsonPath("$.results[2].title").value("Z course"));

        mvc.perform(post("/v2/courses/search?size=3&page=0&sort.field=title&sort.direction=DESC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseIds\": [\"bcourse\", \"Acourse\", \"ZCourse\", \"otherCourse\"], \"query\":  \"course\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(3))
                .andExpect(jsonPath("$.results[0].id").value("ZCourse"))
                .andExpect(jsonPath("$.results[0].title").value("Z course"))
                .andExpect(jsonPath("$.results[1].id").value("bcourse"))
                .andExpect(jsonPath("$.results[1].title").value("b course"))
                .andExpect(jsonPath("$.results[2].id").value("Acourse"))
                .andExpect(jsonPath("$.results[2].title").value("A course"));

        // Not sorted
        mvc.perform(post("/v2/courses/search?size=3&page=0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseIds\": [\"bcourse\", \"Acourse\", \"ZCourse\", \"otherCourse\"], \"query\":  \"course\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(3))
                .andExpect(jsonPath("$.results[0].id").value("bcourse"))
                .andExpect(jsonPath("$.results[0].title").value("b course"))
                .andExpect(jsonPath("$.results[1].id").value("Acourse"))
                .andExpect(jsonPath("$.results[1].title").value("A course"))
                .andExpect(jsonPath("$.results[2].id").value("ZCourse"))
                .andExpect(jsonPath("$.results[2].title").value("Z course"));

        // Invalid sort field
        mvc.perform(post("/v2/courses/search?size=3&page=0&sort.field=invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseIds\": [\"bcourse\", \"Acourse\", \"ZCourse\", \"otherCourse\"], \"query\":  \"course\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCourseSearchPostStartsWith() throws Exception {
        mvc.perform(post("/v2/courses/search?size=3&page=0&sort.field=title&sort.direction=ASC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titleStartsWith\": \"b\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()").value(3))
                .andExpect(jsonPath("$.results[0].id").value("Blended course 1"))
                .andExpect(jsonPath("$.results[0].title").value("Blended course 1"))
                .andExpect(jsonPath("$.results[1].id").value("Blended course 2"))
                .andExpect(jsonPath("$.results[1].title").value("Blended course 2"))
                .andExpect(jsonPath("$.results[2].id").value("Blended course 3"))
                .andExpect(jsonPath("$.results[2].title").value("Blended course 3"));
    }

}
