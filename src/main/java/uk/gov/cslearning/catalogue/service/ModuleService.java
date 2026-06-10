package uk.gov.cslearning.catalogue.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.ELearningModule;
import uk.gov.cslearning.catalogue.domain.module.FileModule;
import uk.gov.cslearning.catalogue.domain.module.Module;
import uk.gov.cslearning.catalogue.domain.module.VideoModule;
import uk.gov.cslearning.catalogue.dto.ModuleDto;
import uk.gov.cslearning.catalogue.dto.factory.ModuleDtoFactory;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.rustici.RusticiEngineService;
import uk.gov.cslearning.catalogue.service.upload.FileUploadService;
import uk.gov.cslearning.catalogue.service.upload.FileUploadServiceFactory;
import uk.gov.cslearning.catalogue.service.upload.UploadServiceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ModuleService {
    private static final int PAGE_SIZE = 10000;

    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final FileUploadServiceFactory fileUploadServiceFactory;
    private final ModuleDtoFactory moduleDtoFactory;
    private final RusticiEngineService rusticiEngineService;

    public ModuleService(CourseRepository courseRepository,
                         CourseService courseService,
                         FileUploadServiceFactory fileUploadServiceFactory,
                         ModuleDtoFactory moduleDtoFactory,
                         RusticiEngineService rusticiEngineService) {
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.fileUploadServiceFactory = fileUploadServiceFactory;
        this.moduleDtoFactory = moduleDtoFactory;
        this.rusticiEngineService = rusticiEngineService;
    }

    public Module save(String courseId, Module module) {
        Course course = courseService.getCourseById(courseId);
        if (module.getModuleType().equals("elearning")) {
            rusticiEngineService.uploadElearningModule(courseId, module.getId(), ((ELearningModule) module).getMediaId());
        }
        course.upsertModule(module);
        courseService.save(course);
        return module;
    }

    public Optional<Module> find(String courseId, String moduleId) {
        return courseService.getCourseById(courseId).getModules().stream()
                .filter(m -> m.getId().equals(moduleId))
                .findFirst();
    }

    public Course updateModule(String courseId, Module newModule) {
        log.info(String.format("Updating module '%s' in course '%s'", newModule.getId(), courseId));
        Course course = courseService.getCourseById(courseId);
        course.upsertModule(newModule);
        courseService.save(course);
        return course;
    }

    public void deleteModule(String courseId, String moduleId) {
        Course course = courseService.getCourseById(courseId);
        Module module = course.getModuleById(moduleId).orElseThrow(ResourceNotFoundException::resourceNotFoundException);

        new Thread(() -> deleteFile(courseId, module)).start();

        course.deleteModule(module);
        courseService.save(course);
    }

    private void deleteFile(String courseId, Module module) {
        FileUploadService fileUploadService;
        if (module instanceof FileModule) {
            String filePath = ((FileModule) module).getUrl();
            fileUploadService = fileUploadServiceFactory.getFileUploadService(UploadServiceType.FILE);
            fileUploadService.delete(filePath);
        } else if (module instanceof VideoModule) {
            String filePath = ((VideoModule) module).getUrl().getPath();
            fileUploadService = fileUploadServiceFactory.getFileUploadService(UploadServiceType.MP4);
            fileUploadService.delete(filePath);
        } else if (module instanceof ELearningModule) {
            ELearningModule EMod = (ELearningModule) module;
            String filePath = EMod.getUrl();
            fileUploadService = fileUploadServiceFactory.getFileUploadService(UploadServiceType.SCORM);
            fileUploadService.deleteDirectory(filePath);
            rusticiEngineService.deleteElearningModule(courseId, EMod.getId());
        }
    }

    public Map<String, ModuleDto> getModuleMap() {
        Map<String, ModuleDto> results = new HashMap<>();

        for (Course course : courseRepository.findAll()) {
            for (Module module : course.getModules()) {
                results.put(module.getId(), moduleDtoFactory.create(module, course));
            }
        }

        return results;
    }

    public Map<String, ModuleDto> getModuleMapForCourseIds(List<String> courseIds) {
        Map<String, ModuleDto> results = new HashMap<>();
        int page = 0;
        int numberOfCourses;
        do {
            Page<Course> courses = courseRepository.findAllByIdIn(courseIds, PageRequest.of(page, PAGE_SIZE));
            courses.forEach(c ->
                    c.getModules().forEach(m ->
                            results.put(m.getId(), moduleDtoFactory.create(m, c))));
            page = page + 1;
            numberOfCourses = courses.getNumberOfElements();
        } while (numberOfCourses == PAGE_SIZE);
        return results;
    }

    public Map<String, ModuleDto> getModuleMapForSupplier(String supplier, Pageable pageable) {
        Map<String, ModuleDto> results = new HashMap<>();
        int page = 0;
        int numberOfCourses;
        do {
            Page<Course> courses = courseRepository.findAllBySupplier(supplier, pageable);
            courses.forEach(c ->
                    c.getModules().forEach(m ->
                            results.put(m.getId(), moduleDtoFactory.create(m, c))));
            page = page + 1;
            numberOfCourses = courses.getNumberOfElements();
        } while (numberOfCourses == PAGE_SIZE);

        return results;
    }
}
