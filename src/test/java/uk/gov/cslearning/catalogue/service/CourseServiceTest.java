package uk.gov.cslearning.catalogue.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.CivilServant.OrganisationalUnit;
import uk.gov.cslearning.catalogue.domain.CivilServant.Profession;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Owner.Owner;
import uk.gov.cslearning.catalogue.domain.Owner.OwnerFactory;
import uk.gov.cslearning.catalogue.domain.Scope;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.domain.Visibility;
import uk.gov.cslearning.catalogue.domain.module.Audience;
import uk.gov.cslearning.catalogue.domain.module.FaceToFaceModule;
import uk.gov.cslearning.catalogue.domain.module.LinkModule;
import uk.gov.cslearning.catalogue.domain.module.Module;
import uk.gov.cslearning.catalogue.domain.validation.CourseValidator;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseStatusRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CourseServiceTest {
    private static final String COURSE_ID_1 = "courseId-1";
    private static final String COURSE_ID_2 = "courseId-2";
    private static final String COURSE_ID_3 = "courseId-3";
    private static final String COURSE_ID_4 = "courseId-4";
    private static final String ORGANISATIONAL_UNIT_CODE = "code";
    private static final PageRequest PAGEABLE = PageRequest.of(0, 10);
    private static final Long PROFESSION_ID = 1L;
    private static final Scope SCOPE = Scope.GLOBAL;
    private static final String TEST_DEPARTMENT_1 = "test-department-1";

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EventService eventService;

    @Mock
    private RegistryService registryService;

    @Mock
    private AuthoritiesService authoritiesService;

    @Mock
    private Authentication authentication;

    @Mock
    private OwnerFactory ownerFactory;

    @Mock
    private CourseValidator courseValidator;

    @Mock
    private ICourseRepository iCourseRepository;

    @Mock
    private ICourseStatusRepository iCourseStatusRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    public void shouldSave() {
        Course course = new Course();

        when(courseRepository.save(course)).thenReturn(course);

        courseService.save(course);

        verify(courseRepository).save(course);
    }

    @Test
    public void shouldFindByIdShouldNotCallEventsAvail() {
        when(courseRepository.findById(COURSE_ID_1)).thenReturn(Optional.empty());

        verify(eventService, times(0)).getEventAvailability(any());
        assertEquals(courseService.findById(COURSE_ID_1), Optional.empty());
    }

    @Test
    public void shouldGetCourseById() {
        Course course = new Course();
        Optional<Course> optionalCourse = Optional.of(course);

        String courseId = "abc123";
        when(courseService.findById(courseId)).thenReturn(optionalCourse);

        assertEquals(courseService.getCourseById(courseId), course);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfCourseDoesntExist() {
        when(courseRepository.findById(COURSE_ID_1)).thenReturn(Optional.empty());

        courseService.getCourseById(COURSE_ID_1);
    }

    @Test
    public void shouldFindCoursesByOrganisationalUnit() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course());
        courses.add(new Course());

        Page<Course> coursesPage = new PageImpl<>(courses);

        when(courseRepository.findAllByOrganisationCode(ORGANISATIONAL_UNIT_CODE, PAGEABLE)).thenReturn(coursesPage);

        assertEquals(courseService.findCoursesByOrganisationalUnit(ORGANISATIONAL_UNIT_CODE, PAGEABLE), coursesPage);

    }

    @Test
    public void shouldFindAllCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course());
        courses.add(new Course());

        Page<Course> coursesPage = new PageImpl<>(courses);

        when(courseRepository.findAll(PAGEABLE)).thenReturn(coursesPage);

        assertEquals(courseService.findAllCourses(PAGEABLE), coursesPage);
    }

    @Test
    public void shouldCreateCourse() {
        Course course = new Course();

        Owner owner = new Owner();
        owner.setOrganisationalUnit(ORGANISATIONAL_UNIT_CODE);
        owner.setProfession(PROFESSION_ID);

        CivilServant civilServant = new CivilServant();
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode(ORGANISATIONAL_UNIT_CODE);
        civilServant.setOrganisationalUnit(organisationalUnit);

        Profession profession = new Profession();
        profession.setId(PROFESSION_ID);
        civilServant.setProfession(profession);

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);
        when(authoritiesService.getScope(authentication)).thenReturn(SCOPE);
        when(ownerFactory.create(civilServant)).thenReturn(owner);
        when(courseRepository.save(course)).thenReturn(course);

        Course createdCourse = courseService.createCourse(course, authentication);
        Owner createdOwner = createdCourse.getOwner();

        verify(courseRepository).save(course);
        assertEquals(createdOwner.getOrganisationalUnit(), ORGANISATIONAL_UNIT_CODE);
        assertEquals(createdOwner.getProfession(), PROFESSION_ID);
    }

    @Test
    public void shouldUpdateCourse() throws MalformedURLException {
        String courseId = "course-id";
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("title1");
        course.setShortDescription("sd1");
        course.setDescription("sd1");
        course.setLearningOutcomes("lc1");

        List<Module> modules = Arrays.asList(new FaceToFaceModule("pc"));
        course.setModules(modules);

        Audience audience = new Audience();
        Set<Audience> audiences = new HashSet<>();
        audiences.add(audience);
        course.setAudiences(audiences);

        course.setPreparation("prep");

        Owner owner = new Owner();
        course.setOwner(owner);

        course.setVisibility(Visibility.PUBLIC);
        course.setStatus(Status.PUBLISHED);

        Course newCourse = new Course();

        String newTitle = "title2";
        newCourse.setTitle(newTitle);
        String newShortDescription = "sd2";
        newCourse.setShortDescription(newShortDescription);
        String newDescription = "sd2";
        newCourse.setDescription(newDescription);
        String newLearningOutcomes = "lc2";
        newCourse.setLearningOutcomes(newLearningOutcomes);

        List<Module> modules2 = Arrays.asList(new LinkModule(new URL("https://www.example.com")));
        newCourse.setModules(modules2);

        Audience audience2 = new Audience();
        Set<Audience> audiences2 = new HashSet<>();
        audiences.add(audience2);
        newCourse.setAudiences(audiences2);

        String prep2 = "prep2";
        newCourse.setPreparation(prep2);

        Owner owner2 = new Owner();
        newCourse.setOwner(owner2);

        Visibility visibility = Visibility.PRIVATE;
        newCourse.setVisibility(visibility);
        Status archived = Status.ARCHIVED;
        newCourse.setStatus(archived);

        uk.gov.cslearning.catalogue.domain.CourseEntity courseEntity = mock(uk.gov.cslearning.catalogue.domain.CourseEntity.class);
        when(iCourseRepository.findByUid(courseId)).thenReturn(Optional.of(courseEntity));
        when(iCourseStatusRepository.findByName(archived.name())).thenReturn(Optional.of(new uk.gov.cslearning.catalogue.domain.CourseStatusEntity(archived.name())));

        Course savedCourse = courseService.updateCourse(course, newCourse);

        verify(courseRepository).save(any());
        verify(iCourseRepository).findByUid(courseId);
        verify(courseEntity).setTitle(newTitle);
        verify(courseEntity).setStatus(any());
        verify(iCourseRepository).save(courseEntity);

        assertEquals(newTitle, savedCourse.getTitle());
        assertEquals(newShortDescription, savedCourse.getShortDescription());
        assertEquals(newDescription, savedCourse.getDescription());
        assertEquals(newLearningOutcomes, savedCourse.getLearningOutcomes());
        assertEquals(prep2, savedCourse.getPreparation());
        assertEquals(owner2, savedCourse.getOwner());
        assertEquals(visibility, savedCourse.getVisibility());
        assertEquals(archived, savedCourse.getStatus());
    }

    @Test
    public void shouldFilterCoursesByAudiencesAndRequiredBy() {
        List<Course> courses = new ArrayList<>();
        Instant oneDay = prepareInstantWithDayDifference(1);

        Course course1 = new Course();
        course1.setId(COURSE_ID_1);
        course1.setAudiences(prepareAudiences(TEST_DEPARTMENT_1, oneDay));
        courses.add(course1);

        Instant sevenDays = prepareInstantWithDayDifference(7);

        Course course2 = new Course();
        course2.setId(COURSE_ID_2);
        course2.setAudiences(prepareAudiences(TEST_DEPARTMENT_1, sevenDays));
        courses.add(course2);

        Instant thirtyDays = prepareInstantWithDayDifference(30);

        Course course3 = new Course();
        course3.setId(COURSE_ID_3);
        course3.setAudiences(prepareAudiences(TEST_DEPARTMENT_1, thirtyDays));
        courses.add(course3);

        Instant twoDays = prepareInstantWithDayDifference(2);

        Course course4 = new Course();
        course4.setId(COURSE_ID_4);
        course4.setAudiences(prepareAudiences(TEST_DEPARTMENT_1, twoDays));
        courses.add(course4);

        when(courseRepository.findAllPublishedRequiredLearning(any(Pageable.class))).thenReturn(courses);

        List<Course> mandatoryCourses = courseService.fetchMandatoryCoursesByDueDate(ImmutableList.of(1L, 7L, 30L));

        assertEquals(mandatoryCourses.size(), 3);
        assertTrue(mandatoryCourses.contains(course1));
        assertTrue(mandatoryCourses.contains(course2));
        assertTrue(mandatoryCourses.contains(course3));
    }

    private Set<Audience> prepareAudiences(String departmentName, Instant requiredBy) {
        Set<String> departments = new HashSet<>();
        departments.add(departmentName);

        Audience audience = new Audience();
        audience.setDepartments(departments);
        audience.setRequiredBy(requiredBy);

        Set<Audience> notRequiredAudiences = new HashSet<>();
        notRequiredAudiences.add(audience);

        return notRequiredAudiences;
    }

    private Instant prepareInstantWithDayDifference(int days) {
        return LocalDate.now()
                .plusDays(days)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }
}
