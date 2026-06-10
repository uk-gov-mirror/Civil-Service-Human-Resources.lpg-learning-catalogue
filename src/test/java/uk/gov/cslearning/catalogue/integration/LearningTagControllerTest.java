package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LearningTagControllerTest extends MySQLIntegrationTestBase {

    @Test
    public void testGetLearningTags() throws Exception {

        mvc.perform(get("/learning-tags")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Project management"))
                .andExpect(jsonPath("$.content[0].description").value("Broad project management skills"))
                .andExpect(jsonPath("$.content[0].code").value("PM"))
                .andExpect(jsonPath("$.content[0].urlSlug").value("project-management"))
                .andExpect(jsonPath("$.content[0].parentId").isEmpty())
                .andExpect(jsonPath("$.content[0].parentName").isEmpty())
                .andExpect(jsonPath("$.content[0].archived").value(false))
                .andExpect(jsonPath("$.content[0].categoryTag").value(true))
                .andExpect(jsonPath("$.content[1].name").value("Tech"))
                .andExpect(jsonPath("$.content[1].description").value("Technical skills"))
                .andExpect(jsonPath("$.content[1].code").value("TECH"))
                .andExpect(jsonPath("$.content[1].urlSlug").value("tech"))
                .andExpect(jsonPath("$.content[1].parentId").isEmpty())
                .andExpect(jsonPath("$.content[1].parentName").isEmpty())
                .andExpect(jsonPath("$.content[1].archived").value(false))
                .andExpect(jsonPath("$.content[1].categoryTag").value(true))
                .andExpect(jsonPath("$.content[2].name").value("Agile"))
                .andExpect(jsonPath("$.content[2].description").value("Agile"))
                .andExpect(jsonPath("$.content[2].code").value("AGILE"))
                .andExpect(jsonPath("$.content[2].urlSlug").value("agile"))
                .andExpect(jsonPath("$.content[2].parentId").value(1))
                .andExpect(jsonPath("$.content[2].parentName").value("Project management"))
                .andExpect(jsonPath("$.content[2].archived").value(false))
                .andExpect(jsonPath("$.content[2].categoryTag").value(true))
                .andExpect(jsonPath("$.content[3].name").value("Software Development"))
                .andExpect(jsonPath("$.content[3].description").value("Designing and writing code "))
                .andExpect(jsonPath("$.content[3].code").value("SOFTDEV"))
                .andExpect(jsonPath("$.content[3].urlSlug").value("software-development"))
                .andExpect(jsonPath("$.content[3].parentId").value(2))
                .andExpect(jsonPath("$.content[3].parentName").value("Tech"))
                .andExpect(jsonPath("$.content[3].archived").value(false))
                .andExpect(jsonPath("$.content[3].categoryTag").value(true))
                .andExpect(jsonPath("$.content[4].name").value("Devops"))
                .andExpect(jsonPath("$.content[4].description").value("Managing and deploying infrastructure"))
                .andExpect(jsonPath("$.content[4].code").value("DEVOPS"))
                .andExpect(jsonPath("$.content[4].urlSlug").value("devops"))
                .andExpect(jsonPath("$.content[4].parentId").value(2))
                .andExpect(jsonPath("$.content[4].parentName").value("Tech"))
                .andExpect(jsonPath("$.content[4].archived").value(false))
                .andExpect(jsonPath("$.content[4].categoryTag").value(true));
    }
}
