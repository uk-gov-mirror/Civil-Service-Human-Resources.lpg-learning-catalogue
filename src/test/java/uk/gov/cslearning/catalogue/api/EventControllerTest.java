package uk.gov.cslearning.catalogue.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.jersey.servlet.WebConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.*;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.EventService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(EventController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, EventController.class})
@EnableSpringDataWebSupport
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private EventService eventService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void shouldAddEventToModule() throws Exception {
        Event event = new Event();
        event.setJoiningInstructions("");
        event.setDateRanges(new ArrayList<>());
        event.setVenue(new Venue("location"));

        String courseId = "course-id";
        String moduleId = "module-id";

        when(courseService.existsById(courseId)).thenReturn(true);

        when(eventService.save(eq(courseId), eq(moduleId), any(Event.class))).thenReturn(event);

        mockMvc.perform(
                        post(String.format("/courses/%s/modules/%s/events", courseId, moduleId)).with(csrf())
                                .content(objectMapper.writeValueAsString(event))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldReturnEvent() throws Exception {
        String courseId = "course-id";
        String moduleId = "module-id";
        String eventId = "event-id";

        Event event = new Event();

        when(courseService.existsById(courseId)).thenReturn(true);
        when(eventService.find(courseId, moduleId, eventId)).thenReturn(Optional.of(event));

        mockMvc.perform(
                        get(String.format("/courses/%s/modules/%s/events/%s", courseId, moduleId, eventId)).with(csrf())
                                .content(objectMapper.writeValueAsString(event))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnNotFoundIfEventNotFound() throws Exception {
        String courseId = "course-id";
        String moduleId = "module-id";
        String eventId = "event-id";

        when(eventService.find(courseId, moduleId, eventId)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get(String.format("/courses/%s/modules/%s/events/%s", courseId, moduleId, eventId)).with(csrf())
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateEvent() throws Exception {

        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.NOON;
        LocalTime end = LocalTime.MIDNIGHT;

        DateRange dateRange = new DateRange();
        dateRange.setDate(date);
        dateRange.setStartTime(start);
        dateRange.setEndTime(end);

        List<DateRange> dateRanges = Collections.singletonList(dateRange);
        Venue venue = new Venue();
        venue.setLocation("venue-location");
        venue.setAddress("venue-address");
        venue.setCapacity(10);
        venue.setMinCapacity(5);

        Course course = new Course();

        Event oldEvent = new Event();
        Event newEvent = new Event();

        newEvent.setJoiningInstructions("new");
        newEvent.setDateRanges(dateRanges);
        newEvent.setVenue(venue);
        oldEvent.setJoiningInstructions("old");

        FaceToFaceModule module = new FaceToFaceModule("product-code");

        HashSet<Event> events = new HashSet<>();
        events.add(oldEvent);
        module.setEvents(events);

        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);

        when(courseService.existsById(course.getId())).thenReturn(true);

        Optional<Course> result = Optional.of(course);
        when(courseService.findById(course.getId())).thenReturn(result);

        when(courseService.save(course)).thenReturn(course);

        mockMvc.perform(
                        put(String.format("/courses/%s/modules/%s/events/%s", course.getId(), module.getId(), oldEvent.getId())).with(csrf())
                                .content(objectMapper.writeValueAsString(newEvent))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Event savedEvent = module.getEvents().stream().filter(e -> e.getId().equals(oldEvent.getId())).findFirst().get();

        assert (module.getEvents().size() == 1);
        assertEquals(savedEvent.getId(), oldEvent.getId());
        assertEquals("new", savedEvent.getJoiningInstructions());
        assertEquals(venue, savedEvent.getVenue());
        assertEquals(dateRange, savedEvent.getDateRanges().get(0));
    }

    @Test
    public void shouldDeleteEvent() throws Exception {
        Course course = new Course();
        FaceToFaceModule module = new FaceToFaceModule("product-code");
        Event event = new Event();

        HashSet<Event> events = new HashSet<>();
        events.add(event);
        module.setEvents(events);

        List<Module> modules = new ArrayList<>();
        modules.add(module);
        course.setModules(modules);

        when(courseService.existsById(course.getId())).thenReturn(true);

        Optional<Course> result = Optional.of(course);
        when(courseService.findById(course.getId())).thenReturn(result);

        when(courseService.save(course)).thenReturn(course);

        mockMvc.perform(
                        delete(String.format("/courses/%s/modules/%s/events/%s", course.getId(), module.getId(), event.getId())).with(csrf()))
                .andExpect(status().isNoContent());

        assert (module.getEvents().isEmpty());
    }
}
