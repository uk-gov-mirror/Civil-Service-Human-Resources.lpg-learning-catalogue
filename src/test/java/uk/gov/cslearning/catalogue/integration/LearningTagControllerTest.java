package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    public void testCreateLearningTag() throws Exception {

        mvc.perform(post("/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test tag 01\", \"urlSlug\":  \"tt01\", \"code\":  \"TT01\", \"isCategoryTag\":  false, \"isArchived\":  false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test tag 01"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.code").value("TT01"))
                .andExpect(jsonPath("$.urlSlug").value("test-tag-01"))
                .andExpect(jsonPath("$.parentId").isEmpty())
                .andExpect(jsonPath("$.parentName").isEmpty())
                .andExpect(jsonPath("$.createdTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.updatedTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.categoryTag").value(false));
    }

    @Test
    public void testCreateLearningTagWithParent() throws Exception {

        mvc.perform(post("/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test & tag 02\", \"urlSlug\":  \"tt01\", \"code\":  \"TT02\", \"parentId\": 1, \"isCategoryTag\":  true, \"isArchived\":  true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test & tag 02"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.code").value("TT02"))
                .andExpect(jsonPath("$.urlSlug").value("test-and-tag-02"))
                .andExpect(jsonPath("$.parentId").value(1))
                .andExpect(jsonPath("$.parentName").value("Project management"))
                .andExpect(jsonPath("$.createdTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.updatedTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.archived").value(true))
                .andExpect(jsonPath("$.categoryTag").value(true));
    }

    @Test
    public void testCreateLearningTagWithInvalidSlug() throws Exception {

        mvc.perform(post("/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test & tag 02\", \"urlSlug\":  \"&£$^\", \"code\":  \"TT02\", \"parentId\": 1, \"isCategoryTag\":  true, \"isArchived\":  true}"))
                .andExpect(status().isBadRequest());
    }
}
