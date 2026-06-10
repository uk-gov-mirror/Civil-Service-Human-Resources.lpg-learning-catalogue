package uk.gov.cslearning.catalogue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.api.SearchResults;
import uk.gov.cslearning.catalogue.api.v2.model.CourseIdAudienceAttributeMap;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.api.v2.model.RequiredLearningIdMap;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Owner.OwnerFactory;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.domain.module.Audience;
import uk.gov.cslearning.catalogue.domain.module.Event;
import uk.gov.cslearning.catalogue.domain.module.FaceToFaceModule;
import uk.gov.cslearning.catalogue.domain.module.Module;
import uk.gov.cslearning.catalogue.domain.validation.CourseValidator;
import uk.gov.cslearning.catalogue.exception.CourseCannotByDeletedException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

@Service
public class CourseService {
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10000);

    private final CourseRepository courseRepository;

    private final EventService eventService;

    private final RegistryService registryService;

    private final OwnerFactory ownerFactory;

    private final AuthoritiesService authoritiesService;

    private final CourseValidator courseValidator;

    public CourseService(CourseRepository courseRepository,
                         EventService eventService, RegistryService registryService, OwnerFactory ownerFactory,
                         AuthoritiesService authoritiesService, CourseValidator courseValidator) {
        this.courseRepository = courseRepository;
        this.eventService = eventService;
        this.registryService = registryService;
        this.ownerFactory = ownerFactory;
        this.authoritiesService = authoritiesService;
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

        course.setOwner(ownerFactory.create(civilServant));
        course.setCreatedTimestamp(LocalDateTime.now(Clock.systemUTC()));

        if (course.getHasBeenPublished() == null) {
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
        
        if (newCourse.getHasBeenPublished() == null) {
            if (newCourse.getStatus().equals(Status.PUBLISHED)) {
                course.setHasBeenPublished(true);
            }
        } else {
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

    public Page<Course> findCoursesBySupplier(Authentication authentication, Pageable pageable) {
        return courseRepository.findAllBySupplier(authoritiesService.getSupplier(authentication), pageable);
    }

    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
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

    public Course deleteCourseById(String courseId) {
        Course course = getCourseById(courseId);
        Boolean courseCanBeDeleted = course.getHasBeenPublished() != null && !course.getHasBeenPublished();

        if (courseCanBeDeleted) {
            courseRepository.delete(course);
            return course;
        } else {
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

    public SearchResults search(CourseSearchParameters params, Pageable pageable) {
        return courseRepository.search(pageable, params);
    }

    public SearchResults search(CourseSearchParameters params, Pageable pageable, String field, Sort.Direction direction) {
        if (field != null && direction != null) {
            return courseRepository.search(pageable, params, field, direction);
        } else {
            return this.search(params, pageable);
        }
    }

    public CourseIdAudienceAttributeMap getCourseIdAudienceAttributeMap() {
        List<Course> allCourses = courseRepository.findAll(PageRequest.of(0, 10000)).getContent();
        Map<String, Set<String>> areaOfWorkMap = new HashMap<>();
        Map<String, Set<String>> departmentCodeMap = new HashMap<>();
        Map<String, Set<String>> interestMap = new HashMap<>();
        allCourses
                .stream().filter(c -> c.getStatus().equals(Status.PUBLISHED))
                .forEach(c -> c.getAudiences()
                        .stream().filter(a -> a.getType() != null && a.getType().equals(Audience.Type.OPEN))
                        .forEach(a -> {
                            a.getAreasOfWork().forEach(aow -> areaOfWorkMap.merge(aow, new HashSet<>(Collections.singletonList(c.getId())), (existingIds, newIds) -> {
                                existingIds.addAll(newIds);
                                return existingIds;
                            }));
                            a.getDepartments().forEach(dep -> departmentCodeMap.merge(dep, new HashSet<>(Collections.singletonList(c.getId())), (existingIds, newIds) -> {
                                existingIds.addAll(newIds);
                                return existingIds;
                            }));
                            a.getInterests().forEach(interest -> interestMap.merge(interest, new HashSet<>(Collections.singletonList(c.getId())), (existingIds, newIds) -> {
                                existingIds.addAll(newIds);
                                return existingIds;
                            }));
                        }));
        return new CourseIdAudienceAttributeMap(areaOfWorkMap, departmentCodeMap, interestMap);
    }
}
