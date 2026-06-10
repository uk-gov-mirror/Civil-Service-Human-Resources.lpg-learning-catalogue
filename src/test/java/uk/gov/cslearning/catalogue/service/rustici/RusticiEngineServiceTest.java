package uk.gov.cslearning.catalogue.service.rustici;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cslearning.catalogue.domain.CustomMediaMetadata;
import uk.gov.cslearning.catalogue.domain.Media;
import uk.gov.cslearning.catalogue.dto.rustici.course.Course;
import uk.gov.cslearning.catalogue.dto.rustici.course.CreateCourse;
import uk.gov.cslearning.catalogue.dto.rustici.course.CreateCourseResponse;
import uk.gov.cslearning.catalogue.repository.elastic.MediaRepository;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RusticiEngineServiceTest {

    @Mock
    private MediaRepository mediaRepository;
    @Mock
    private CSLToRusticiDataService rusticiDataService;
    @Mock
    private RusticiEngineClient rusticiEngineClient;
    @InjectMocks
    private RusticiEngineService rusticiEngineService;

    private final String courseId = "courseId";
    private final String moduleId = "moduleId";
    private final String mediaId = "mediaId";
    private final String manifestFile = "imsmanifest.xml";

    @Test
    public void testSuccessfullyUploadElearningModule() {
        String rusticiCourseId = String.format("%s.%s", courseId, moduleId);

        Course course = mock(Course.class);
        when(course.getTitle()).thenReturn("Test title");
        when(course.getCourseLearningStandard()).thenReturn("SCORM2");
        CreateCourse createCourse = mock(CreateCourse.class);
        CreateCourseResponse createCourseResponse = mock(CreateCourseResponse.class);
        when(createCourseResponse.getCourse()).thenReturn(course);

        Media media = mock(Media.class);
        when(media.getMetadataWithCustomKey(CustomMediaMetadata.ELEARNING_MANIFEST)).thenReturn(manifestFile);
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));
        when(rusticiDataService.getCreateCourseData(courseId, mediaId, manifestFile)).thenReturn(createCourse);
        when(rusticiDataService.getRusticiCourseId(courseId, moduleId)).thenReturn(rusticiCourseId);
        when(rusticiEngineClient.createCourse(createCourse, rusticiCourseId)).thenReturn(createCourseResponse);

        rusticiEngineService.uploadElearningModule(courseId, moduleId, mediaId);
        verify(rusticiEngineClient).createCourse(createCourse, rusticiCourseId);
    }

    @Test
    public void testSuccessfullyDeleteElearningModule() {
        String rusticiCourseId = String.format("%s.%s", courseId, moduleId);
        when(rusticiDataService.getRusticiCourseId(courseId, moduleId)).thenReturn(rusticiCourseId);
        rusticiEngineService.deleteElearningModule(courseId, moduleId);
        verify(rusticiEngineClient).deleteCourse(rusticiCourseId);
    }
}
