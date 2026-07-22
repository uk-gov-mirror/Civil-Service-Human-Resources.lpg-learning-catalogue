package uk.gov.cslearning.catalogue.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.*;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

@Slf4j
@Service
public class CourseLearningTagService {

    private final ICourseRepository courseRepository;
    private final ILearningTagRepository learningTagRepository;
    private final ICourseTagRepository courseTagRepository;
    private final LearningTagFactory learningTagFactory;
    private final CourseService courseService;

    public CourseLearningTagService(ICourseRepository courseRepository, ILearningTagRepository learningTagRepository, ICourseTagRepository courseTagRepository, LearningTagFactory learningTagFactory, CourseService courseService) {
        this.courseRepository = courseRepository;
        this.learningTagRepository = learningTagRepository;
        this.courseTagRepository = courseTagRepository;
        this.learningTagFactory = learningTagFactory;
        this.courseService = courseService;
    }

    public CourseLearningTagDto addLearningTagToCourse(String courseUid, LearningTagDto learningTagDto) {
        log.info("Adding learning tag {} to course {}", learningTagDto.getId(), courseUid);
        CourseEntity courseEntity = courseRepository.findByUid(courseUid)
                .orElseGet(() -> {
                    try {
                        Course course = courseService.getCourseById(courseUid);
                        return courseRepository.save(new CourseEntity(course.getId(), course.getTitle()));
                    } catch (IllegalStateException e) {
                        throw new ResourceNotFoundException(String.format("Course with UID %s not found", courseUid));
                    }
                });

        LearningTag learningTag = learningTagRepository.findById(learningTagDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Learning tag with ID %s not found", learningTagDto.getId())));

        CourseLearningTagEntity courseLearningTagEntity = new CourseLearningTagEntity(learningTag, courseEntity);
        courseTagRepository.save(courseLearningTagEntity);

        LearningTagDto tagDto = learningTagFactory.createDto(learningTag);
        log.info("Added learning tag {} to course {}", learningTagDto.getId(), courseUid);
        return new CourseLearningTagDto(courseEntity.getId(), courseEntity.getUid(), courseEntity.getTitle(), tagDto);
    }

    public void removeLearningTagFromCourse(String courseUid, Long learningTagId) {
        log.info("Removing learning tag {} from course {}", learningTagId, courseUid);
        CourseEntity courseEntity = courseRepository.findByUid(courseUid)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Course with UID %s not found", courseUid)));

        CourseLearningTagId courseLearningTagId = new CourseLearningTagId(learningTagId, courseEntity.getId());
        if (!courseTagRepository.existsById(courseLearningTagId)) {
            throw new ResourceNotFoundException(String.format("Association between course %s and learning tag %s not found", courseUid, learningTagId));
        }
        courseTagRepository.deleteById(courseLearningTagId);
        log.info("Removed learning tag {} from course {}", learningTagId, courseUid);
    }
}
