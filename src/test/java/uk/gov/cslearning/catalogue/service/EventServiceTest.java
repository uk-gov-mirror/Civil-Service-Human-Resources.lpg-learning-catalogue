package uk.gov.cslearning.catalogue.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.*;
import uk.gov.cslearning.catalogue.dto.CourseDto;
import uk.gov.cslearning.catalogue.dto.EventDto;
import uk.gov.cslearning.catalogue.dto.ModuleDto;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.record.LearnerRecordService;

import java.util.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private EventDtoMapService eventDtoMapService;

    @Mock
    private LearnerRecordService learnerRecordService;

    @InjectMocks
    private EventService eventService;

    @Test
    public void shouldSaveEvent() {
        String courseId = "courseId";
        String moduleId = "moduleId";

        Event newEvent = new Event();

        Course course = new Course();
        course.setId(courseId);

        Module module = new FaceToFaceModule("product-code");
        module.setId(moduleId);

        Event savedEvent = new Event();
        Collection<Event> events = new HashSet<>();
        events.add(savedEvent);
        ((FaceToFaceModule) module).setEvents(events);

        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        Assert.assertEquals(eventService.save(courseId, moduleId, newEvent), newEvent);

        verify(courseRepository).findById(courseId);
        verify(courseRepository).save(course);
    }

    @Test
    public void shouldFindEvent() {
        String courseId = "courseId";
        String moduleId = "moduleId";

        Course course = new Course();
        course.setId(courseId);

        Module module = new FaceToFaceModule("product-code");
        module.setId(moduleId);

        Event savedEvent = new Event();
        Venue venue = new Venue();
        venue.setCapacity(10);
        savedEvent.setVenue(venue);

        Collection<Event> events = new HashSet<>();
        events.add(savedEvent);
        ((FaceToFaceModule) module).setEvents(events);

        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);

        Integer confirmedBookings = 1;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(learnerRecordService.getEventActiveBookingsCount(savedEvent.getId())).thenReturn(confirmedBookings);

        int availability = (venue.getCapacity() - confirmedBookings);

        Optional<Event> result = eventService.find(courseId, moduleId, savedEvent.getId());

        Assert.assertEquals(result, Optional.of(savedEvent));
        Assert.assertTrue(result.get().getVenue().getAvailability() == availability);

        verify(courseRepository).findById(courseId);
        verify(learnerRecordService).getEventActiveBookingsCount(savedEvent.getId());
    }

    @Test
    public void shouldGetEventStatus() {
        String eventId = "eventId";

        EventStatus eventStatus = EventStatus.ACTIVE;

        when(learnerRecordService.getEventStatus(eventId)).thenReturn(eventStatus);

        Assert.assertEquals(eventService.getStatus(eventId), eventStatus);

        verify(learnerRecordService).getEventStatus(eventId);
    }

    @Test
    public void shouldGetCancellationReason() {
        String eventId = "eventId";

        CancellationReason cancellationReason = CancellationReason.UNAVAILABLE;

        when(learnerRecordService.getCancellationReason(eventId)).thenReturn(cancellationReason);

        Assert.assertEquals(eventService.getCancellationReason(eventId), cancellationReason);

        verify(learnerRecordService).getCancellationReason(eventId);
    }

    @Test
    public void shouldGetEventMap() {
        String courseTitle = "course-title";
        String courseId = "course-id";
        String moduleId = "module-id";
        String moduleTitle = "module-title";
        String eventId = "event-id";

        Course course = new Course();
        course.setTitle(courseTitle);
        course.setId(courseId);

        CourseDto courseDto = new CourseDto();
        courseDto.setTitle(courseTitle);
        courseDto.setId(courseId);

        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setId(moduleId);
        moduleDto.setTitle(moduleTitle);
        moduleDto.setCourse(courseDto);

        EventDto eventDto = new EventDto();
        eventDto.setId(eventId);
        eventDto.setModule(moduleDto);

        List<Course> courses = Collections.singletonList(course);
        when(courseRepository.findAll()).thenReturn(courses);

        Map<String, EventDto> stringEventDtoMap = new HashMap<>();
        stringEventDtoMap.put(eventId, eventDto);

        when(eventDtoMapService.getStringEventDtoMap(courses)).thenReturn(stringEventDtoMap);
        Assert.assertEquals(eventService.getEventMap(), stringEventDtoMap);
    }

    @Test
    public void shouldGetEventMapBySupplier() {
        String supplier = "SUPPLIER";
        Pageable unpaged = Pageable.unpaged();

        String courseTitle = "course-title";
        String courseId = "course-id";
        String moduleId = "module-id";
        String moduleTitle = "module-title";
        String eventId = "event-id";

        Course course = new Course();
        course.setTitle(courseTitle);
        course.setId(courseId);

        CourseDto courseDto = new CourseDto();
        courseDto.setTitle(courseTitle);
        courseDto.setId(courseId);

        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setId(moduleId);
        moduleDto.setTitle(moduleTitle);
        moduleDto.setCourse(courseDto);

        EventDto eventDto = new EventDto();
        eventDto.setId(eventId);
        eventDto.setModule(moduleDto);

        Page<Course> courses = Page.empty();
        when(courseRepository.findAllBySupplier(supplier, unpaged)).thenReturn(courses);

        Map<String, EventDto> stringEventDtoMap = new HashMap<>();
        stringEventDtoMap.put(eventId, eventDto);

        when(eventDtoMapService.getStringEventDtoMapForSupplier(courses)).thenReturn(stringEventDtoMap);


        Assert.assertEquals(eventService.getEventMapBySupplier(supplier, unpaged), stringEventDtoMap);
    }
}
