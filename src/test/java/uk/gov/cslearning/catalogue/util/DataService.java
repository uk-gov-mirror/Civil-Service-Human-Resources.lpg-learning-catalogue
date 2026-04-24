package uk.gov.cslearning.catalogue.util;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.collect.Set;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Owner.Owner;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.domain.Visibility;
import uk.gov.cslearning.catalogue.domain.module.*;
import uk.gov.cslearning.catalogue.repository.CourseRepository;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataService {

    @Getter
    private final CourseRepository repository;
    public DataService(CourseRepository repository) {
        this.repository = repository;
    }

    public void loadBulkCourses() {
        log.info("Loading bulk courses");
        repository.saveAll(createBulkCourses());
    }

    public void deleteBulkCourses() {
        log.info("Tearing down bulk courses");
        repository.deleteAll();
    }

    public List<Course> createBulkCourses() {
        List<Course> courses = new ArrayList<>();

        // Required learning

        Course requiredCourse1 = createCourse("Required course 1");
        requiredCourse1.setId("req-course-1");
        requiredCourse1.setAudiences(Set.of(
                createRequiredLearningAudience(Set.of("HMRC", "CO")),
                createAudience(Set.of("DWP"), Collections.emptySet(), Collections.emptySet(), Collections.emptySet())
        ));
        courses.add(requiredCourse1);

        Course requiredCourse2 = createCourse("Required course 2");
        requiredCourse2.setId("req-course-2");
        Audience requiredAudience = createRequiredLearningAudience(Set.of("HMRC"));
        requiredAudience.setRequiredBy(Instant.now().plus(7L, ChronoUnit.DAYS));
        requiredCourse2.setAudiences(Set.of(
                requiredAudience,
                createAudience(Set.of("CO"), Collections.emptySet(), Collections.emptySet(), Collections.emptySet())
        ));
        courses.add(requiredCourse2);

        // Archived courses

        Course archived1 = createCourse("Archived course");
        archived1.setModules(Collections.singletonList(
                createELearningModule()
        ));
        archived1.setAudiences(Set.of(createAudience(Collections.emptyList(), Set.of("Analysis"), Collections.emptyList(), Collections.emptyList())));
        archived1.setStatus(Status.ARCHIVED);
        courses.add(archived1);

        Course archived2 = createCourse("Archived course 2");
        archived2.setModules(Collections.singletonList(
                createFileModule()
        ));
        archived2.setStatus(Status.ARCHIVED);
        archived2.setAudiences(Set.of(createAudience(Collections.emptyList(), Collections.emptyList(), Set.of("EU"), Collections.emptyList())));
        courses.add(archived2);

        // Private courses

        Course private1 = createCourse("Private course 1");
        private1.setModules(Collections.singletonList(
                createFileModule()
        ));
        private1.setStatus(Status.PUBLISHED);
        private1.setVisibility(Visibility.PRIVATE);
        private1.setAudiences(Set.of(createAudience(Collections.emptyList(), Set.of("Finance"), Collections.emptyList(), Collections.emptyList())));
        courses.add(private1);

        // Single module courses

        Course elearningCourse = createCourse("ELearning course");
        elearningCourse.setId("elearning-course");
        elearningCourse.setModules(Collections.singletonList(
                createELearningModule()
        ));
        Owner o = new Owner();
        o.setSupplier("KPMG");
        elearningCourse.setOwner(o);
        elearningCourse.setAudiences(Set.of(createAudience(Collections.emptyList(), Set.of("Analysis"), Collections.emptyList(), Collections.emptyList())));
        courses.add(elearningCourse);

        Course fileCourse = createCourse("File course");
        fileCourse.setModules(Collections.singletonList(
                createFileModule()
        ));
        fileCourse.setAudiences(Set.of(createAudience(Collections.emptyList(), Collections.emptyList(), Set.of("EU"), Collections.emptyList())));
        courses.add(fileCourse);

        Course linkCourse = createCourse("Link course");
        linkCourse.setModules(Collections.singletonList(
                createLinkModule()
        ));
        linkCourse.setAudiences(Set.of(createAudience(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Set.of("G7"))));
        courses.add(linkCourse);

        Course videoCourse = createCourse("Video course");
        videoCourse.setModules(Collections.singletonList(
                createVideoModule()
        ));
        videoCourse.setAudiences(Set.of(createAudience(Set.of("COD"), Collections.emptyList(), Collections.emptyList(), Collections.emptyList())));
        courses.add(videoCourse);

        Course paidFaceToFaceCourse = createCourse("Paid face to face course");
        paidFaceToFaceCourse.setModules(Collections.singletonList(
                createFaceToFaceModule(false)
        ));
        paidFaceToFaceCourse.setCostFromModules();
        courses.add(paidFaceToFaceCourse);

        Course freeFaceToFaceCourse = createCourse("Free face to face course");
        freeFaceToFaceCourse.setModules(Collections.singletonList(
                createFaceToFaceModule(true)
        ));
        freeFaceToFaceCourse.setCostFromModules();
        courses.add(freeFaceToFaceCourse);

        // Blended courses (with audiences)

        Course blended1 = createCourse("Blended course 1");
        blended1.setModules(Lists.newArrayList(createELearningModule(),
                createFileModule(),
                createLinkModule()));
        blended1.setAudiences(Set.of(createAudience(Collections.emptyList(), Set.of("Analysis"), Set.of("EU", "Parliament"), Set.of("G7"))));
        courses.add(blended1);

        Course blended2 = createCourse("Blended course 2");
        blended2.setModules(Lists.newArrayList(createELearningModule(),
                createFileModule(),
                createFaceToFaceModule(false)));
        blended2.setCostFromModules();
        blended2.setAudiences(Set.of(createAudience(Set.of("DWP", "COD"), Set.of("Finance"), Collections.emptyList(), Set.of("G7", "G6"))));
        courses.add(blended2);

        Course blended3 = createCourse("Blended course 3");
        blended3.setModules(Lists.newArrayList(createELearningModule(),
                createLinkModule(),
                createELearningModule()));
        blended3.setAudiences(Set.of(createAudience(Set.of("MOD"), Collections.emptyList(), Collections.emptyList(), Set.of("G7")),
                createAudience(Set.of("COD"), Set.of("DDaT"), Collections.emptyList(), Set.of("G6"))));
        courses.add(blended3);

        // Other

        Course otherCourse1 = createCourse("Learning 1");
        otherCourse1.setModules(Lists.newArrayList(createELearningModule(),
                createLinkModule(),
                createELearningModule()));
        otherCourse1.setAudiences(Set.of(createAudience(Set.of("MOD"), Collections.emptyList(), Collections.emptyList(), Set.of("G7")),
                createAudience(Set.of("COD"), Set.of("DDaT"), Collections.emptyList(), Set.of("G6"))));
        courses.add(otherCourse1);

        return courses;
    }

    public Course createCourse(String title, String shortDesc,  String longDesc, Visibility visibility, Status status) {
        Course course = new Course(title, shortDesc, longDesc, visibility);
        course.setId(title);
        course.setStatus(status);
        course.setOwner(new Owner());
        return course;
    }

    public Course createCourse(String title, Visibility visibility, Status status) {
        return createCourse(title, String.format("%s short description", title), String.format("%s long description", title), visibility, status);
    }

    public Course createCourse(String title) {
        return createCourse(title, Visibility.PUBLIC, Status.PUBLISHED);
    }

    // Module

    public ELearningModule createELearningModule() {
        ELearningModule elearningModule = new ELearningModule("http://startPage", "http://url.com");
        elearningModule.setTitle("ELearning module");
        elearningModule.setDescription("An ELearning module");
        elearningModule.setDuration(100L);
        elearningModule.setOptional(false);
        return elearningModule;
    }

    @SneakyThrows
    public VideoModule createVideoModule() {
        VideoModule videoModule = new VideoModule(new URL("http://startPage"));
        videoModule.setTitle("Video module");
        videoModule.setDescription("A Video module");
        videoModule.setDuration(100L);
        videoModule.setOptional(false);
        return videoModule;
    }

    @SneakyThrows
    public LinkModule createLinkModule() {
        LinkModule linkModule = new LinkModule(new URL("http://startPage"));
        linkModule.setTitle("Link module");
        linkModule.setDescription("A Link module");
        linkModule.setDuration(100L);
        linkModule.setOptional(false);
        return linkModule;
    }

    public FileModule createFileModule() {
        FileModule linkModule = new FileModule("http://startPage", 1000L);
        linkModule.setTitle("File module");
        linkModule.setDescription("A File module");
        linkModule.setDuration(100L);
        linkModule.setOptional(false);
        return linkModule;
    }

    public FaceToFaceModule createFaceToFaceModule(boolean free) {
        FaceToFaceModule linkModule = new FaceToFaceModule("productCode");
        linkModule.setTitle("File module");
        linkModule.setDescription("A File module");
        linkModule.setDuration(100L);
        if (!free) {
            linkModule.setCost(BigDecimal.valueOf(100L));
        }
        linkModule.setOptional(false);
        return linkModule;
    }

    // Audience

    public List<Audience> createRequiredLearningAudiences(Collection<Collection<String>> departments) {
        return departments.stream().map(DataService::createRequiredLearningAudience).collect(Collectors.toList());
    }

    public static Audience createRequiredLearningAudience(Collection<String> departments) {
        Audience a = createAudience(departments, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        a.setType(Audience.Type.REQUIRED_LEARNING);
        a.setRequiredBy(Instant.now());
        return a;
    }

    public static Audience createAudience(Collection<String> departments, Collection<String> areasOfWork, Collection<String> interests,
                                          Collection<String> grades) {
        Audience a = new Audience();
        a.setType(Audience.Type.OPEN);
        a.setDepartments(Set.copyOf(departments));
        a.setAreasOfWork(Set.copyOf(areasOfWork));
        a.setInterests(Set.copyOf(interests));
        a.setGrades(Set.copyOf(grades));
        return a;
    }


}
