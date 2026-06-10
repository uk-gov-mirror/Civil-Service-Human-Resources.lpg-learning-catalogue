package uk.gov.cslearning.catalogue.service;

import com.google.common.collect.Maps;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.CancellationReason;
import uk.gov.cslearning.catalogue.domain.module.Event;
import uk.gov.cslearning.catalogue.domain.module.EventStatus;
import uk.gov.cslearning.catalogue.domain.module.FaceToFaceModule;
import uk.gov.cslearning.catalogue.dto.EventDto;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.record.LearnerRecordService;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final CourseRepository courseRepository;
    private final LearnerRecordService learnerRecordService;
    private final EventDtoMapService eventDtoMapService;

    public EventService(CourseRepository courseRepository, LearnerRecordService learnerRecordService,
                        EventDtoMapService eventDtoMapService) {
        this.courseRepository = courseRepository;
        this.learnerRecordService = learnerRecordService;
        this.eventDtoMapService = eventDtoMapService;
    }

    private Course getCourseById(String courseId) {
        return courseRepository.findById(courseId).orElseThrow((Supplier<IllegalStateException>) () -> {
            throw new IllegalStateException(
                    String.format("Unable to find course. Course does not exist: %s", courseId));
        });
    }

    public Event save(String courseId, String moduleId, Event event) {
        Course course = getCourseById(courseId);

        FaceToFaceModule module = (FaceToFaceModule) course.getModuleById(moduleId)
                .orElseThrow(ResourceNotFoundException::resourceNotFoundException);

        if (module == null) {
            throw new IllegalStateException(
                    String.format("Unable to add event. Module does not exist: %s", moduleId));
        }

        Collection<Event> events = module.getEvents();

        HashSet<Event> newEvents = new HashSet<>();

        for (Event e : events) {
            newEvents.add(e);
        }

        event.setStatus(EventStatus.ACTIVE);
        newEvents.add(event);

        module.setEvents(newEvents);
        courseRepository.save(course);

        return event;
    }

    public Optional<Event> find(String courseId, String moduleId, String eventId) {
        Course course = getCourseById(courseId);

        FaceToFaceModule module = (FaceToFaceModule) course.getModuleById(moduleId)
                .orElseThrow(ResourceNotFoundException::resourceNotFoundException);

        if (module == null) {
            throw new IllegalStateException(
                    String.format("Unable to find event: %s. Module does not exist: %s", eventId, moduleId));
        }

        Optional<Event> result = module.getEvents().stream().filter(e -> e.getId().equals(eventId)).findFirst();

        if (result.isPresent()) {
            Event event = result.get();
            event = getEventAvailability(event);
            event.setStatus(getStatus(eventId));

            if (event.getStatus() == EventStatus.CANCELLED) {
                event.setCancellationReason(getCancellationReason(eventId));
            }
        }

        return result;
    }

    public Event getEventAvailability(Event event) {
        Integer activeBookings = learnerRecordService.getEventActiveBookingsCount(event.getId());
        event.getVenue().setAvailability(event.getVenue().getCapacity() - activeBookings);
        return event;
    }

    public EventStatus getStatus(String eventId) {
        return learnerRecordService.getEventStatus(eventId);
    }

    public CancellationReason getCancellationReason(String eventId) {
        return learnerRecordService.getCancellationReason(eventId);
    }

    public Map<String, EventDto> getEventMap() {
        Iterable<Course> courses = courseRepository.findAll();

        return eventDtoMapService.getStringEventDtoMap(courses);
    }

    public Map<String, EventDto> getEventMapBySupplier(String supplier, Pageable pageable) {
        Iterable<Course> courses = courseRepository.findAllBySupplier(supplier, pageable);

        return eventDtoMapService.getStringEventDtoMapForSupplier(courses);
    }

    public Collection<Event> updateEventsWithLearnerRecordData(Collection<Event> events) {
        List<String> eventUids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<String, uk.gov.cslearning.catalogue.service.record.model.Event> lrEventMap = Maps
                .uniqueIndex(learnerRecordService.getEvents(eventUids, true), uk.gov.cslearning.catalogue.service.record.model.Event::getUid);

        events.forEach(e -> {
            e.getVenue().setAvailability(e.getVenue().getCapacity());
            uk.gov.cslearning.catalogue.service.record.model.Event lrEvent = lrEventMap.get(e.getId());
            if (lrEvent != null) {
                e.getVenue().setAvailability(e.getVenue().getCapacity() - lrEvent.getActiveBookingCount());
            }
        });
        return events;
    }
}
