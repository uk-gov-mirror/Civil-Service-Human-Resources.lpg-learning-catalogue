package uk.gov.cslearning.catalogue.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.*;
import uk.gov.cslearning.catalogue.dto.ModuleDto;
import uk.gov.cslearning.catalogue.dto.factory.CourseDtoFactory;
import uk.gov.cslearning.catalogue.dto.factory.ModuleDtoFactory;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.rustici.RusticiEngineService;
import uk.gov.cslearning.catalogue.service.upload.FileUploadService;
import uk.gov.cslearning.catalogue.service.upload.FileUploadServiceFactory;
import uk.gov.cslearning.catalogue.service.upload.ScormFileUploadService;
import uk.gov.cslearning.catalogue.service.upload.UploadServiceType;

import java.net.URI;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private FileUploadServiceFactory fileUploadServiceFactory;

    @Mock
    private ModuleDtoFactory moduleDtoFactory;

    @Mock
    private RusticiEngineService rusticiEngineService;

    @InjectMocks
    private ModuleService moduleService;

    @Test
    public void shouldSaveModuleToCourse() throws Exception {
        String courseId = "course-id";
        Module module = new LinkModule(new URI("http://localhost").toURL());
        Course course = new Course();

        when(courseService.getCourseById(courseId)).thenReturn(course);

        assertEquals(module, moduleService.save(courseId, module));
        assertEquals(Collections.singletonList(module), course.getModules());
        verify(courseService).save(course);
    }

    @Test
    public void shouldSaveElearningModuleToCourse() {
        String courseId = "course-id";
        ELearningModule module = new ELearningModule("", "http://test");
        module.setMediaId("media-id");
        module.setId("module-id");
        Course course = new Course();

        when(courseService.getCourseById(courseId)).thenReturn(course);

        assertEquals(module, moduleService.save(courseId, module));
        assertEquals(Collections.singletonList(module), course.getModules());
        verify(courseService).save(course);
        verify(rusticiEngineService).uploadElearningModule(courseId, "module-id", "media-id");
    }

    @Test
    public void shouldReturnModule() throws Exception {
        String courseId = "course-id";

        Module module1 = new LinkModule(new URI("http://module1").toURL());
        Module module2 = new LinkModule(new URI("http://module2").toURL());
        Module module3 = new LinkModule(new URI("http://module3").toURL());

        Course course = new Course();
        course.setModules(Arrays.asList(module1, module2, module3));

        when(courseService.getCourseById(courseId)).thenReturn(course);

        Optional<Module> result = moduleService.find(courseId, module2.getId());

        assertTrue(result.isPresent());
        assertEquals(module2, result.get());
    }

    @Test
    public void shouldUpdateModule() throws Exception {
        String moduleId = "moduleId";
        String courseId = "course-id";
        String url = "https://www.example.com";
        String updatedTitle = "title-updated";
        Course course = new Course();
        Module module = new LinkModule(new URL(url));
        module.setId(moduleId);
        module.setTitle("title");
        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);
        Module newModule = new LinkModule(new URL(url));
        newModule.setId(moduleId);
        newModule.setTitle(updatedTitle);
        when(courseService.getCourseById(courseId)).thenReturn(course);
        moduleService.updateModule(courseId, newModule);
        assertEquals(course.getModuleById(moduleId).orElseThrow(ResourceNotFoundException::new).getTitle(), updatedTitle);
    }

    @Test
    public void shouldDeleteModule() throws Exception {
        String moduleId = "moduleId";
        String courseId = "courseId";
        String url = "https://www.example.org";
        Course course = new Course();
        Module module = new LinkModule(new URL(url));
        module.setId(moduleId);
        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);
        when(courseService.getCourseById(courseId)).thenReturn(course);
        moduleService.deleteModule(courseId, moduleId);
        assertEquals(0, course.getModules().size());
    }

    @Test
    public void shouldDeleteModuleAndFile() throws Exception {
        String moduleId = "moduleId";
        String courseId = "courseId";
        String url = "test/path/to/file.pdf";
        Course course = new Course();
        Module module = new FileModule(url, (long) 1024);
        module.setId(moduleId);
        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);

        FileUploadService fileUploadService = mock(FileUploadService.class);
        when(fileUploadServiceFactory.getFileUploadService(UploadServiceType.FILE)).thenReturn(fileUploadService);
        when(courseService.getCourseById(courseId)).thenReturn(course);
        moduleService.deleteModule(courseId, moduleId);
        assertEquals(0, course.getModules().size());
        verify(fileUploadService, timeout(2000)).delete(url);
    }

    @Test
    public void shouldDeleteElearningModuleAndFile() throws Exception {
        String moduleId = "moduleId";
        String mediaId = "mediaId";
        String courseId = "courseId";
        String url = "test/path/to/file.pdf";
        Course course = new Course();
        ELearningModule module = new ELearningModule("", url);
        module.setMediaId(mediaId);
        module.setId(moduleId);
        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);

        ScormFileUploadService fileUploadService = mock(ScormFileUploadService.class);
        when(fileUploadServiceFactory.getFileUploadService(UploadServiceType.SCORM)).thenReturn(fileUploadService);
        when(courseService.getCourseById(courseId)).thenReturn(course);
        moduleService.deleteModule(courseId, moduleId);
        assertEquals(0, course.getModules().size());
        verify(fileUploadService, timeout(2000)).deleteDirectory(url);
        verify(rusticiEngineService).deleteElearningModule(courseId, moduleId);
    }

    @Test
    public void shouldReturnMapOfIdAndModule() {
        Module module1 = new FaceToFaceModule("a");

        Course course1 = new Course();
        course1.setModules(Collections.singletonList(module1));

        Module module2 = new FaceToFaceModule("b");
        Module module3 = new FaceToFaceModule("c");

        Course course2 = new Course();
        course2.setModules(Arrays.asList(module2, module3));

        when(courseRepository.findAll()).thenReturn(Arrays.asList(course1, course2));

        ModuleDto moduleDto1 = new ModuleDto();
        ModuleDto moduleDto2 = new ModuleDto();
        ModuleDto moduleDto3 = new ModuleDto();

        when(moduleDtoFactory.create(module1, course1)).thenReturn(moduleDto1);
        when(moduleDtoFactory.create(module2, course2)).thenReturn(moduleDto2);
        when(moduleDtoFactory.create(module3, course2)).thenReturn(moduleDto3);

        Map<String, ModuleDto> expected = ImmutableMap.of(
                module1.getId(), moduleDto1,
                module2.getId(), moduleDto2,
                module3.getId(), moduleDto3
        );

        assertEquals(expected, moduleService.getModuleMap());
    }

    @Test
    public void shouldReturnModuleMapForGivenCourseIds() {
        List<String> courseIds = Arrays.asList("course1-id", "course2-id");

        Module module1 = new FileModule("", 1L);
        module1.setTitle("module1-title");
        module1.setOptional(false);
        module1.setAssociatedLearning(false);
        Course course1 = new Course();
        course1.setId("course1-id");
        course1.setTitle("course1-title");
        course1.setTopicId("course1-topic-id");
        course1.setModules(Collections.singletonList(module1));

        Module module2 = new FaceToFaceModule("product-code");
        module2.setTitle("module2-title");
        module2.setOptional(true);
        module2.setAssociatedLearning(true);

        Module module3 = new ELearningModule("start-page", "url");
        module3.setTitle("module3-title");
        module3.setOptional(false);
        module3.setAssociatedLearning(false);

        Course course2 = new Course();
        course2.setId("course2-id");
        course2.setTitle("course2-title");
        course2.setTopicId("course2-topic-id");
        course2.setModules(Arrays.asList(module2, module3));

        PageImpl<Course> pageImpl = new PageImpl<>(Arrays.asList(course1, course2));
        when(courseRepository.findAllByIdIn(courseIds, PageRequest.of(0, 10000))).thenReturn(pageImpl);

        ModuleDtoFactory moduleDtoFactory1 = new ModuleDtoFactory(new CourseDtoFactory());
        ModuleDto moduleDto1 = moduleDtoFactory1.create(module1, course1);
        ModuleDto moduleDto2 = moduleDtoFactory1.create(module2, course2);
        ModuleDto moduleDto3 = moduleDtoFactory1.create(module3, course2);
        when(moduleDtoFactory.create(module1, course1)).thenReturn(moduleDto1);
        when(moduleDtoFactory.create(module2, course2)).thenReturn(moduleDto2);
        when(moduleDtoFactory.create(module3, course2)).thenReturn(moduleDto3);

        Map<String, ModuleDto> expected = ImmutableMap.of(
                module1.getId(), moduleDto1,
                module2.getId(), moduleDto2,
                module3.getId(), moduleDto3
        );

        Map<String, ModuleDto> result = moduleService.getModuleMapForCourseIds(courseIds);

        assertEquals(expected, result);
    }
}
