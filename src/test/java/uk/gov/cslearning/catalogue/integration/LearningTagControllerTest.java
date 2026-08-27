package uk.gov.cslearning.catalogue.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.cslearning.catalogue.domain.*;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseStatusRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagHyperlinkRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LearningTagControllerTest extends MySQLIntegrationTestBase {

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ICourseStatusRepository courseStatusRepository;

    @Autowired
    private ILearningTagRepository learningTagRepository;

    @Autowired
    private ILearningTagHyperlinkRepository learningTagHyperlinkRepository;

    @Autowired
    private ICourseTagRepository courseTagRepository;

    @Autowired
    private CourseRepository elasticCourseRepository;

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
                .andExpect(jsonPath("$.content[0].category").value(true))
                .andExpect(jsonPath("$.content[1].name").value("Tech"))
                .andExpect(jsonPath("$.content[1].description").value("Technical skills"))
                .andExpect(jsonPath("$.content[1].code").value("TECH"))
                .andExpect(jsonPath("$.content[1].urlSlug").value("tech"))
                .andExpect(jsonPath("$.content[1].parentId").isEmpty())
                .andExpect(jsonPath("$.content[1].parentName").isEmpty())
                .andExpect(jsonPath("$.content[1].archived").value(false))
                .andExpect(jsonPath("$.content[1].category").value(true))
                .andExpect(jsonPath("$.content[2].name").value("Agile"))
                .andExpect(jsonPath("$.content[2].description").value("Agile"))
                .andExpect(jsonPath("$.content[2].code").value("AGILE"))
                .andExpect(jsonPath("$.content[2].urlSlug").value("agile"))
                .andExpect(jsonPath("$.content[2].parentId").value(1))
                .andExpect(jsonPath("$.content[2].parentName").value("Project management"))
                .andExpect(jsonPath("$.content[2].archived").value(false))
                .andExpect(jsonPath("$.content[2].category").value(true))
                .andExpect(jsonPath("$.content[3].name").value("Software Development"))
                .andExpect(jsonPath("$.content[3].description").value("Designing and writing code "))
                .andExpect(jsonPath("$.content[3].code").value("SOFTDEV"))
                .andExpect(jsonPath("$.content[3].urlSlug").value("software-development"))
                .andExpect(jsonPath("$.content[3].parentId").value(2))
                .andExpect(jsonPath("$.content[3].parentName").value("Tech"))
                .andExpect(jsonPath("$.content[3].archived").value(false))
                .andExpect(jsonPath("$.content[3].category").value(true))
                .andExpect(jsonPath("$.content[4].name").value("Devops"))
                .andExpect(jsonPath("$.content[4].description").value("Managing and deploying infrastructure"))
                .andExpect(jsonPath("$.content[4].code").value("DEVOPS"))
                .andExpect(jsonPath("$.content[4].urlSlug").value("devops"))
                .andExpect(jsonPath("$.content[4].parentId").value(2))
                .andExpect(jsonPath("$.content[4].parentName").value("Tech"))
                .andExpect(jsonPath("$.content[4].archived").value(false))
                .andExpect(jsonPath("$.content[4].category").value(true));
    }

    @Test
    @Transactional
    public void testCreateLearningTag() throws Exception {

        mvc.perform(post("/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test tag 01\", \"urlSlug\":  \"tt01\", \"code\":  \"TT01\", \"category\":  false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test tag 01"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.code").value("TT01"))
                .andExpect(jsonPath("$.urlSlug").value("tt01"))
                .andExpect(jsonPath("$.parentId").isEmpty())
                .andExpect(jsonPath("$.parentName").isEmpty())
                .andExpect(jsonPath("$.createdTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.updatedTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.category").value(false));
    }

    @Test
    @Transactional
    public void testCreateLearningTagWithParent() throws Exception {

        mvc.perform(post("/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test & tag 02\", \"urlSlug\":  \"tt02\", \"code\":  \"TT02\", \"parentId\": 1, \"category\":  true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test & tag 02"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.code").value("TT02"))
                .andExpect(jsonPath("$.urlSlug").value("tt02"))
                .andExpect(jsonPath("$.parentId").value(1))
                .andExpect(jsonPath("$.parentName").value("Project management"))
                .andExpect(jsonPath("$.createdTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.updatedTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.category").value(true));
    }

    @Test
    public void testCreateLearningTagWithInvalidSlug() throws Exception {

        mvc.perform(post("/learning-tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test & tag 02\", \"urlSlug\":  \"&£$^\", \"code\":  \"TT02\", \"parentId\": 1, \"category\":  true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void testUpdateLearningTag() throws Exception {

        mvc.perform(put("/learning-tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test tag 01 edit\", \"urlSlug\":  \"tt01-edit\", \"code\":  \"TT01E\", \"category\":  true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test tag 01 edit"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.code").value("TT01E"))
                .andExpect(jsonPath("$.urlSlug").value("tt01-edit"))
                .andExpect(jsonPath("$.parentId").isEmpty())
                .andExpect(jsonPath("$.parentName").isEmpty())
                .andExpect(jsonPath("$.updatedTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.category").value(true));
    }

    @Test
    @Transactional
    public void testUpdateLearningTagWithParent() throws Exception {

        mvc.perform(put("/learning-tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"test tag 01 edit\", \"urlSlug\":  \"tt01-edit\", \"code\":  \"TT01E\", \"category\":  true, \"parentId\":  2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test tag 01 edit"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.code").value("TT01E"))
                .andExpect(jsonPath("$.urlSlug").value("tt01-edit"))
                .andExpect(jsonPath("$.parentId").value(2))
                .andExpect(jsonPath("$.parentName").value("Tech"))
                .andExpect(jsonPath("$.updatedTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.category").value(true));
    }

    @Test
    @Transactional
    public void testUpdateLearningTagUnsetParent() throws Exception {

        mvc.perform(put("/learning-tags/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":  \"Agile\", \"urlSlug\":  \"agile\", \"code\":  \"AGILE\", \"category\":  true, \"parentId\":  null}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Agile"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.code").value("AGILE"))
                .andExpect(jsonPath("$.urlSlug").value("agile"))
                .andExpect(jsonPath("$.parentId").isEmpty())
                .andExpect(jsonPath("$.parentName").isEmpty())
                .andExpect(jsonPath("$.updatedTimestamp").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.category").value(true));
    }

    @Test
    @Transactional
    public void testGetCoursesByLearningTag() throws Exception {
        CourseStatusEntity draftStatus = courseStatusRepository.findByName("Draft").get();
        CourseEntity course1 = courseRepository.save(new CourseEntity("uid-b", "Course B", "Course B short description", draftStatus));
        CourseEntity course2 = courseRepository.save(new CourseEntity("uid-a", "Course A", "Course A short description", draftStatus));
        CourseEntity course3 = courseRepository.save(new CourseEntity("uid-c", "Course C", "Course C short description", draftStatus));

        LearningTag tag = learningTagRepository.findById(1L).get();

        courseTagRepository.save(new CourseLearningTagEntity(tag, course1));
        courseTagRepository.save(new CourseLearningTagEntity(tag, course2));
        courseTagRepository.save(new CourseLearningTagEntity(tag, course3));

        mvc.perform(get("/learning-tags/1/courses?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("Course A"))
                .andExpect(jsonPath("$.content[0].id").value("uid-a"))
                .andExpect(jsonPath("$.content[0].shortDescription").value("Course A short description"))
                .andExpect(jsonPath("$.content[0].status").value("Draft"))
                .andExpect(jsonPath("$.content[1].title").value("Course B"))
                .andExpect(jsonPath("$.content[1].id").value("uid-b"))
                .andExpect(jsonPath("$.content[1].shortDescription").value("Course B short description"))
                .andExpect(jsonPath("$.content[1].status").value("Draft"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalResults").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @Transactional
    public void testUpdateLearningTagState() throws Exception {

        mvc.perform(put("/learning-tags/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":  \"ARCHIVE\", \"ids\": [1, 2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulUpdates[0]").value(1))
                .andExpect(jsonPath("$.successfulUpdates[1]").value(2))
                .andExpect(jsonPath("$.failedUpdates").isEmpty());
    }

    @Test
    @Transactional
    public void testRemoveCoursesFromLearningTag() throws Exception {
        CourseStatusEntity draftStatus = courseStatusRepository.findByName("Draft").get();
        CourseEntity course1 = courseRepository.save(new CourseEntity("uid-1", "Course 1", "Course 1 short description", draftStatus));
        CourseEntity course2 = courseRepository.save(new CourseEntity("uid-2", "Course 2", "Course 2 short description", draftStatus));

        LearningTag tag = learningTagRepository.findById(1L).get();

        courseTagRepository.save(new CourseLearningTagEntity(tag, course1));
        courseTagRepository.save(new CourseLearningTagEntity(tag, course2));

        mvc.perform(delete("/learning-tags/1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\": [\"uid-1\", \"uid-2\", \"non-existent\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulIds", hasSize(2)))
                .andExpect(jsonPath("$.successfulIds[0]").value("uid-1"))
                .andExpect(jsonPath("$.successfulIds[1]").value("uid-2"))
                .andExpect(jsonPath("$.failedIds", hasSize(1)))
                .andExpect(jsonPath("$.failedIds[0]").value("non-existent"));
    }

    @Test
    @Transactional
    public void testAssignCoursesToTag() throws Exception {
        CourseStatusEntity draftStatus = courseStatusRepository.findByName("Draft").get();

        courseRepository.save(new CourseEntity("uid-existing", "Old Title", "Old short description", draftStatus));

        Course elasticCourse = new Course();
        elasticCourse.setId("uid-new");
        elasticCourse.setTitle("New Course");
        elasticCourse.setShortDescription("New Course short description");
        elasticCourse.setStatus(Status.DRAFT);

        when(elasticCourseRepository.findById("uid-new")).thenReturn(java.util.Optional.of(elasticCourse));
        when(elasticCourseRepository.findById("uid-missing")).thenReturn(java.util.Optional.empty());

        String requestBody = "{" +
                "\"learningTagIds\": [1, 2]," +
                "\"courseIds\": [\"uid-existing\", \"uid-new\", \"uid-missing\"]" +
                "}";

        mvc.perform(post("/learning-tags/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isCreated());

        // Verify tags for uid-existing
        mvc.perform(get("/learning-tags/1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='uid-existing')]").exists());
        mvc.perform(get("/learning-tags/2/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='uid-existing')]").exists());

        // Verify tags for uid-new
        mvc.perform(get("/learning-tags/1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='uid-new')]").exists());
        mvc.perform(get("/learning-tags/2/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='uid-new')]").exists());
    }

    @Test
    @Transactional
    public void testGetHyperlinksByLearningTag() throws Exception {
        LearningTag tag1 = learningTagRepository.findById(1L).get();

        learningTagHyperlinkRepository.save(new LearningTagHyperlink(tag1, "https://news.sky.com/uk", "Sky news", "Sky news website"));
        learningTagHyperlinkRepository.save(new LearningTagHyperlink(tag1, "https://www.bbc.co.uk/news", "BBC news", "BBC news website"));
        learningTagHyperlinkRepository.save(new LearningTagHyperlink(tag1, "https://stackoverflow.com/questions", "Stack overflow questions", "Stack overflow questions website"));

        mvc.perform(get("/learning-tags/1/hyperlinks?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("BBC news"))
                .andExpect(jsonPath("$.content[0].href").value("https://www.bbc.co.uk/news"))
                .andExpect(jsonPath("$.content[0].description").value("BBC news website"))
                .andExpect(jsonPath("$.content[0].id").isNumber())
                .andExpect(jsonPath("$.content[1].title").value("Sky news"))
                .andExpect(jsonPath("$.content[1].href").value("https://news.sky.com/uk"))
                .andExpect(jsonPath("$.content[1].description").value("Sky news website"))
                .andExpect(jsonPath("$.content[1].id").isNumber())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalResults").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.pageable").exists());

        mvc.perform(get("/learning-tags/1/hyperlinks?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Stack overflow questions"))
                .andExpect(jsonPath("$.content[0].href").value("https://stackoverflow.com/questions"))
                .andExpect(jsonPath("$.content[0].description").value("Stack overflow questions website"))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalResults").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @Transactional
    public void testRemoveHyperlinksFromLearningTag() throws Exception {
        LearningTag tag1 = learningTagRepository.findById(1L).get();

        LearningTagHyperlink h1 = learningTagHyperlinkRepository.save(new LearningTagHyperlink(tag1, "https://news.sky.com/uk", "Sky news", "Sky news website"));
        LearningTagHyperlink h2 = learningTagHyperlinkRepository.save(new LearningTagHyperlink(tag1, "https://www.bbc.co.uk/news", "BBC news", "BBC news website"));

        mvc.perform(delete("/learning-tags/1/hyperlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"ids\": [%d, %d, 99999]}", h1.getId(), h2.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulIds", hasSize(2)))
                .andExpect(jsonPath("$.successfulIds[0]").value(h1.getId()))
                .andExpect(jsonPath("$.successfulIds[1]").value(h2.getId()))
                .andExpect(jsonPath("$.failedIds", hasSize(1)))
                .andExpect(jsonPath("$.failedIds[0]").value(99999));
    }
}
