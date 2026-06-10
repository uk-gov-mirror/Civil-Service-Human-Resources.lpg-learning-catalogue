package uk.gov.cslearning.catalogue.service.upload;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.CivilServant.OrganisationalUnit;
import uk.gov.cslearning.catalogue.domain.CivilServant.Profession;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Owner.Owner;
import uk.gov.cslearning.catalogue.domain.module.Audience;
import uk.gov.cslearning.catalogue.service.AudienceService;
import uk.gov.cslearning.catalogue.service.AuthoritiesService;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.RegistryService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AudienceServiceTest {
    private static final String COURSE_ID = "abc123";
    private static final String ORGANISATIONAL_UNIT_CODE = "code";
    private static final String PROFESSION_NAME = "name";

    @Mock
    private Authentication authentication;

    @Mock
    private CourseService courseService;

    @Mock
    private AuthoritiesService authoritiesService;

    @Mock
    private RegistryService registryService;

    @InjectMocks
    private AudienceService audienceService;

    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSave() {
        Course course = new Course();
        Set<Audience> oldAudiences = new HashSet<>();
        course.setAudiences(oldAudiences);

        Audience audience = new Audience();
        Set<Audience> audiences = new HashSet<>();
        audiences.add(audience);
        course.setAudiences(audiences);

        when(courseService.getCourseById(any())).thenReturn(course);
        when(courseService.save(any())).thenReturn(course);

        Course savedCourse = audienceService.save(COURSE_ID, audience);

        assertEquals(savedCourse.getAudiences(), audiences);
    }

    @Test
    public void shouldUpdateAudience() {
        Course course = new Course();
        Set<Audience> audiences = new HashSet<>();
        Audience audience = new Audience();
        audiences.add(audience);
        course.setAudiences(audiences);

        Set<Audience> newAudiences = new HashSet<>();
        Audience newAudience = new Audience();
        audiences.add(newAudience);

        Course returnedCourse = new Course();
        returnedCourse.setAudiences(newAudiences);

        when(courseService.save(course)).thenReturn(returnedCourse);

        Course updatedCourse = audienceService.updateAudience(course, newAudience, audience);

        assertEquals(updatedCourse.getAudiences(), newAudiences);
    }

    @Test
    public void shouldFindAudience() {
        Course course = new Course();
        Audience audience = new Audience();
        audience.setId("123");
        Set<Audience> audiences = new HashSet<>();
        audiences.add(audience);
        course.setAudiences(audiences);

        when(courseService.getCourseById(any())).thenReturn(course);

        Optional<Audience> returnedAudience = audienceService.find(COURSE_ID, audience.getId());

        assertTrue(returnedAudience.isPresent());
        assertEquals(returnedAudience.get(), audience);
    }

    @Test
    public void shouldReturnEmptyOptionalIfNoAudienceFound() {
        Course course = new Course();

        when(courseService.getCourseById(any())).thenReturn(course);

        Optional<Audience> returnedAudience = audienceService.find(COURSE_ID, "123");

        assertFalse(returnedAudience.isPresent());
    }

    @Test
    public void shouldReturnTrueIfIsCslAuthor() {
        when(authoritiesService.isCslAuthor(authentication)).thenReturn(true);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), true);

        verify(courseService, times(0)).getCourseById(COURSE_ID);
    }

    @Test
    public void shouldReturnTrueIfIsLearningManager() {
        when(authoritiesService.isCslAuthor(authentication)).thenReturn(false);
        when(authoritiesService.isLearningManager(authentication)).thenReturn(true);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), true);

        verify(courseService, times(0)).getCourseById(COURSE_ID);
    }

    @Test
    public void shouldReturnFalseIfCourseHasNoOwner() {
        Course course = new Course();
        course.setId(COURSE_ID);

        when(authoritiesService.isCslAuthor(authentication)).thenReturn(false);

        when(courseService.getCourseById(any())).thenReturn(course);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), false);

        verify(registryService, times(0)).getCurrentCivilServant();
    }

    @Test
    public void shouldReturnFalseIfNotOrgAuthorOrProfessionAuthor() {
        CivilServant civilServant = new CivilServant();
        Course course = new Course();
        course.setId(COURSE_ID);

        Owner owner = new Owner();
        course.setOwner(owner);

        when(authoritiesService.isCslAuthor(authentication)).thenReturn(false);

        when(courseService.getCourseById(any())).thenReturn(course);
        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);
        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(false);
        when(authoritiesService.isProfessionAuthor(authentication)).thenReturn(false);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), false);
    }

    @Test
    public void shouldReturnFalseIfIsOrgAuthorButDoesntMatchAndNotProfessionAuthor() {
        CivilServant civilServant = new CivilServant();
        Course course = new Course();
        course.setId(COURSE_ID);

        Owner owner = new Owner();
        course.setOwner(owner);

        when(authoritiesService.isCslAuthor(authentication)).thenReturn(false);

        when(courseService.getCourseById(any())).thenReturn(course);
        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(true);
        when(authoritiesService.isOrganisationalUnitCodeEqual(civilServant, course.getOwner())).thenReturn(false);
        when(authoritiesService.isProfessionAuthor(authentication)).thenReturn(false);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), false);
    }

    @Test
    public void shouldReturnTrueIfMatchingOrgButNotProfessionAuthor() {
        CivilServant civilServant = new CivilServant();
        Course course = new Course();
        course.setId(COURSE_ID);

        Owner owner = new Owner();
        course.setOwner(owner);

        when(authoritiesService.isCslAuthor(authentication)).thenReturn(false);

        when(courseService.getCourseById(any())).thenReturn(course);
        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(true);
        when(authoritiesService.isOrganisationalUnitCodeEqual(civilServant, course.getOwner())).thenReturn(true);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), true);
    }

    @Test
    public void shouldFalseIfIsProfAuthorButDoesntMatchAndNotOrganisation() {
        CivilServant civilServant = new CivilServant();
        Course course = new Course();
        course.setId(COURSE_ID);

        Owner owner = new Owner();
        course.setOwner(owner);

        when(authoritiesService.isCslAuthor(authentication)).thenReturn(false);

        when(courseService.getCourseById(any())).thenReturn(course);
        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(false);
        when(authoritiesService.isProfessionAuthor(authentication)).thenReturn(true);
        when(authoritiesService.isProfessionIdEqual(civilServant, course.getOwner())).thenReturn(false);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), false);
    }

    @Test
    public void shouldReturnTrueIfIsProfAuthorAndMatchesMatchAndNotOrganisation() {
        CivilServant civilServant = new CivilServant();
        Course course = new Course();
        course.setId(COURSE_ID);

        Owner owner = new Owner();
        course.setOwner(owner);

        when(authoritiesService.isCslAuthor(authentication)).thenReturn(false);

        when(courseService.getCourseById(any())).thenReturn(course);
        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(false);
        when(authoritiesService.isProfessionAuthor(authentication)).thenReturn(true);
        when(authoritiesService.isProfessionIdEqual(civilServant, course.getOwner())).thenReturn(true);

        assertEquals(audienceService.isPermitted(COURSE_ID, authentication), true);
    }

    @Test
    public void shouldSetDefaultsIfIsOrgAuthorWithCode() {
        Audience audience = new Audience();

        CivilServant civilServant = new CivilServant();
        OrganisationalUnit organisationalUnit = new OrganisationalUnit();
        organisationalUnit.setCode(ORGANISATIONAL_UNIT_CODE);
        civilServant.setOrganisationalUnit(organisationalUnit);

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(true);

        Audience returnedAudience = audienceService.setDefaults(authentication, audience);

        verify(authoritiesService, times(0)).isProfessionAuthor(authentication);
        assertTrue(returnedAudience.getDepartments().contains(ORGANISATIONAL_UNIT_CODE));
    }

    @Test
    public void shouldSetDefaultsIfIsOrgAuthorWithoutCode() {
        Audience audience = new Audience();

        CivilServant civilServant = new CivilServant();

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(true);

        Audience returnedAudience = audienceService.setDefaults(authentication, audience);

        verify(authoritiesService, times(0)).isProfessionAuthor(authentication);
        assertFalse(returnedAudience.getDepartments().contains(ORGANISATIONAL_UNIT_CODE));
    }

    @Test
    public void shouldSetDefaultsIfIsProfAuthorWithCode() {
        Audience audience = new Audience();

        CivilServant civilServant = new CivilServant();
        Profession profession = new Profession();
        profession.setName(PROFESSION_NAME);
        civilServant.setProfession(profession);

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(false);
        when(authoritiesService.isProfessionAuthor(authentication)).thenReturn(true);

        Audience returnedAudience = audienceService.setDefaults(authentication, audience);

        assertTrue(returnedAudience.getAreasOfWork().contains(PROFESSION_NAME));
        verify(authoritiesService).isOrgAuthor(authentication);
    }

    @Test
    public void shouldSetDefaultsIfIsProfAuthorWithoutName() {
        Audience audience = new Audience();

        CivilServant civilServant = new CivilServant();

        when(registryService.getCurrentCivilServant()).thenReturn(civilServant);

        when(authoritiesService.isOrgAuthor(authentication)).thenReturn(false);
        when(authoritiesService.isProfessionAuthor(authentication)).thenReturn(true);

        Audience returnedAudience = audienceService.setDefaults(authentication, audience);

        assertFalse(returnedAudience.getAreasOfWork().contains(PROFESSION_NAME));
        verify(authoritiesService).isOrgAuthor(authentication);
    }
}
