package uk.gov.cslearning.catalogue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.api.v2.model.RequiredLearningIdMap;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.CivilServant.OrganisationalUnit;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Owner.OwnerFactory;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.domain.module.Audience;
import uk.gov.cslearning.catalogue.domain.module.Event;
import uk.gov.cslearning.catalogue.domain.module.FaceToFaceModule;
import uk.gov.cslearning.catalogue.domain.module.Module;
import uk.gov.cslearning.catalogue.domain.validation.CourseValidator;
import uk.gov.cslearning.catalogue.exception.CourseCannotByDeletedException;
import uk.gov.cslearning.catalogue.repository.CourseRepository;
import uk.gov.cslearning.catalogue.repository.CourseRequiredRepository;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10000);

    private final CourseRepository courseRepository;

    private final CourseRequiredRepository courseRequiredRepository;

    private final EventService eventService;

    private final RegistryService registryService;

    private final OwnerFactory ownerFactory;

    private final AuthoritiesService authoritiesService;

    private final RequiredByService requiredByService;

    private final CourseValidator courseValidator;

    public CourseService(CourseRepository courseRepository, CourseRequiredRepository courseRequiredRepository,
                         EventService eventService, RegistryService registryService, OwnerFactory ownerFactory,
                         AuthoritiesService authoritiesService, RequiredByService requiredByService,
                         CourseValidator courseValidator) {
        this.courseRepository = courseRepository;
        this.courseRequiredRepository = courseRequiredRepository;
        this.eventService = eventService;
        this.registryService = registryService;
        this.ownerFactory = ownerFactory;
        this.authoritiesService = authoritiesService;
        this.requiredByService = requiredByService;
        this.courseValidator = courseValidator;
    }

    public Course save(Course course) {
        course.setCostFromModules();
        return courseRepository.save(course);
    }

    public Course createCourse(Course course, Authentication authentication) {
        CivilServant civilServant = registryService.getCurrentCivilServant();

        civilServant.setScope(authoritiesService.getScope(authentication));

        civilServant.setSupplier(authoritiesService.getSupplier(authentication));

        course.setOwner(ownerFactory.create(civilServant, course));
        course.setCreatedTimestamp(LocalDateTime.now(Clock.systemUTC()));

        if(course.getHasBeenPublished() == null){
            course.setHasBeenPublished(course.getStatus().equals(Status.PUBLISHED));
        }

        courseRepository.save(course);

        return course;
    }

    public Course updateCourse(String courseId, Course newCourse) {
        Course course = getCourseById(courseId);
        return updateCourse(course, newCourse);
    }

    public Course updateCourse(Course course, Course newCourse) {
         courseValidator.validate(course, newCourse);
        course.setTitle(newCourse.getTitle());
        course.setShortDescription(newCourse.getShortDescription());
        course.setLearningOutcomes(newCourse.getLearningOutcomes());
        course.setPreparation(newCourse.getPreparation());
        course.setVisibility(newCourse.getVisibility());
        course.setStatus(newCourse.getStatus());
        course.setDescription(newCourse.getDescription());
        course.setTopicId(newCourse.getTopicId());
        course.setUpdatedTimestamp(LocalDateTime.now(Clock.systemUTC()));
        Optional.ofNullable(newCourse.getLearningProvider()).ifPresent(course::setLearningProvider);

        if(newCourse.getHasBeenPublished() == null){
            if(newCourse.getStatus().equals(Status.PUBLISHED)){
                course.setHasBeenPublished(true);
            }
        }
        else{
            course.setHasBeenPublished(newCourse.getHasBeenPublished());
        }

        courseRepository.save(course);
        return course;
    }

    public void updateCourseModules(String courseId, List<Module> modules) {
        Course course = getCourseById(courseId);
        course.setModules(modules);
        courseRepository.save(course);
    }

    public Optional<Course> findById(String courseId) {
        return this.findById(courseId, false);
    }

    public Optional<Course> findById(String courseId, boolean includeAvailability) {
        return courseRepository.findById(courseId)
                .map(c -> {
                    if (includeAvailability) {
                        return getCourseEventsAvailability(c);
                    } else {
                        return c;
                    }
                });
    }

    public Course getCourseById(String courseId) throws IllegalStateException {
        return this.getCourseById(courseId, false);
    }

    public Course getCourseById(String courseId, boolean includeAvailability) throws IllegalStateException {
        return findById(courseId, includeAvailability)
                .orElseThrow((Supplier<IllegalStateException>) () -> {
                    throw new IllegalStateException(
                            String.format("Unable to find course. Course does not exist: %s", courseId));
                });
    }

    public Page<Course> findCoursesByOrganisationalUnit(String organisationalUnitCode, Pageable pageable) {
        return courseRepository.findAllByOrganisationCode(organisationalUnitCode, pageable);
    }

    public Page<Course> findCoursesByProfession(String professionId, Pageable pageable) {
        return courseRepository.findAllByProfessionId(professionId, pageable);
    }

    public Page<Course> findCoursesBySupplier(Authentication authentication, Pageable pageable) {
        return courseRepository.findAllBySupplier(authoritiesService.getSupplier(authentication), pageable);
    }

    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public List<String> getOrganisationParents(String departments) {
        List<String> list = new ArrayList<>();
        if (departments != null && !departments.equals("NONE")) {
            List<String> collect = Arrays.stream(departments.split(",")).collect(Collectors.toList());
            collect.forEach(s -> {
                for (OrganisationalUnit organisationalUnit : registryService.getOrganisationalUnit(s)) {
                    if (organisationalUnit != null) {
                        String code = organisationalUnit.getCode();
                        list.add(code);
                    }
                }
            });
        }
        return list;
    }

    public Map<String, List<String>> getOrganisationParentsMap() {
        return registryService.getOrganisationalUnitParentsMap();
    }

    public boolean isCourseRequiredWithinRangeForOrg(Course course, List<String> organisationalUnitList, long from, long to) {
        List<Audience> orgAudiences = course.getAudiences()
                .stream()
                .filter(audience -> organisationalUnitList.stream().anyMatch(organisationalUnit -> audience.getDepartments().contains(organisationalUnit)))
                .collect(Collectors.toList());

        return orgAudiences
                .stream()
                .anyMatch(audience -> requiredByService.isAudienceRequiredWithinRange(audience, Instant.now(), from, to));
    }

    public List<Course> fetchMandatoryCoursesByDueDate(Collection<Long> days) {
        LocalDate now = LocalDate.now();

        Map<Course, Set<Audience>> alterAudienceList = new HashMap();

        courseRepository.findAllPublishedRequiredLearning(DEFAULT_PAGEABLE)
            .forEach(course -> course.getAudiences()
                .forEach(audience -> addCourseIfAudienceIsRequired(course, audience, alterAudienceList, days, now)));

        for (Course course : alterAudienceList.keySet()) {
            course.setAudiences(alterAudienceList.get(course));
        }

        return new ArrayList(alterAudienceList.keySet());
    }

    public Page<Course> prepareCoursePage(Pageable pageable, List<Course> courses) {
        Set<String> courseSet = new HashSet<>();
        List<Course> filteredCourses = courses.stream()
            .skip(pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .filter(course -> courseSet.add(course.getId()))
            .collect(Collectors.toList());

        return new PageImpl<>(filteredCourses, pageable, courses.size());
    }

    public Map<String, List<Course>> groupByOrganisationCode(List<Course> courses) {
        Map<String, List<Course>> groupedCourses = new HashMap<>();

        for (Course course : courses) {
            for (Audience audience : course.getAudiences()) {
                addToGroupedCourses(course, groupedCourses, audience);
            }
        }

        return groupedCourses;
    }

    public RequiredLearningIdMap getDepartmentCodeToCourseIdRequiredLearningMap() {
        List<Course> allRequiredLearning = courseRepository.findAllPublishedRequiredLearning(PageRequest.of(0, 10000));
        Map<String, List<String>> depCodeToCourseIdsMap = new HashMap<>();
        allRequiredLearning.forEach(c -> c.getMandatoryDepartmentCodes().forEach(dep -> {
            List<String> courseIds = depCodeToCourseIdsMap.get(dep);
            if (courseIds == null) {
                courseIds = new ArrayList<>();
            }
            courseIds.add(c.getId());
            depCodeToCourseIdsMap.put(dep, courseIds);
        }));
        return new RequiredLearningIdMap(depCodeToCourseIdsMap);
    }

    public Page<Course> getRequiredCourses(String profession, String gradeCode, List<String>departments, List<String>otherAreasOfWork,  List<String>interests, String courseStatus,  Pageable pageable) {
        return courseRequiredRepository.findRequired(profession, gradeCode, departments, otherAreasOfWork, interests, courseStatus, pageable);
    }

    public Course deleteCourseById(String courseId){
        Course course = getCourseById(courseId);
        Boolean courseCanBeDeleted = course.getHasBeenPublished() != null && !course.getHasBeenPublished();

        if(courseCanBeDeleted){
            courseRepository.delete(course);
            return course;
        }
        else{
            throw new CourseCannotByDeletedException();
        }

    }

    private void addToGroupedCourses(Course course, Map<String, List<Course>> groupedCourses, Audience audience) {
        for (String department : audience.getDepartments()) {
            if (!groupedCourses.containsKey(department)) {
                List<Course> departmentCourse = new ArrayList<>();
                departmentCourse.add(course);
                groupedCourses.put(department, departmentCourse);
            } else {
                groupedCourses.get(department).add(course);
            }
        }
    }

    private void addCourseIfAudienceIsRequired(Course course,
            Audience audience,
            Map<Course, Set<Audience>> alterAudienceList,
            Collection<Long> days,
            LocalDate now) {
        if (isAudienceRequired(audience, days, now)) {
            if (alterAudienceList.containsKey(course)) {
                alterAudienceList.get(course).add(audience);
            } else {
                Set<Audience> newAudienceList = new HashSet();
                newAudienceList.add(audience);
                alterAudienceList.put(course, newAudienceList);
            }
        }
    }

    private boolean isAudienceRequired(Audience audience, Collection<Long> days, LocalDate now) {
        return audience.getRequiredBy() != null
            && audience.getDepartments() != null
            && isRequiredDateDue(LocalDateTime.ofInstant(audience.getRequiredBy(), ZoneId.systemDefault()).toLocalDate(), days, now);
    }

    private boolean isRequiredDateDue(LocalDate requiredBy, Collection<Long> days, LocalDate now) {
        return days.contains(ChronoUnit.DAYS.between(now, requiredBy));
    }

    private Course getCourseEventsAvailability(Course course) {

        course.getModules().forEach(module -> {
            if (module instanceof FaceToFaceModule) {
                Collection<Event> moduleEvents = ((FaceToFaceModule) module).getEvents();
                eventService.updateEventsWithLearnerRecordData(moduleEvents);
            }
        });

        return course;
    }
}
