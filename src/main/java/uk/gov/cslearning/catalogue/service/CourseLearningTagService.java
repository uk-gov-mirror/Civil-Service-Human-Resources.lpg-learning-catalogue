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

    public CourseLearningTagService(ICourseRepository courseRepository, ILearningTagRepository learningTagRepository, ICourseTagRepository courseTagRepository, LearningTagFactory learningTagFactory) {
        this.courseRepository = courseRepository;
        this.learningTagRepository = learningTagRepository;
        this.courseTagRepository = courseTagRepository;
        this.learningTagFactory = learningTagFactory;
    }

    public CourseLearningTagDto addLearningTagToCourse(Long courseId, LearningTagDto learningTagDto) {
        log.info("Adding learning tag {} to course {}", learningTagDto.getId(), courseId);
        CourseEntity courseEntity = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Course with ID %s not found", courseId)));

        LearningTag learningTag = learningTagRepository.findById(learningTagDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Learning tag with ID %s not found", learningTagDto.getId())));

        CourseLearningTagEntity courseLearningTagEntity = new CourseLearningTagEntity(learningTag, courseEntity);
        courseTagRepository.save(courseLearningTagEntity);

        LearningTagDto tagDto = learningTagFactory.createDto(learningTag);
        log.info("Added learning tag {} to course {}", learningTagDto.getId(), courseId);
        return new CourseLearningTagDto(courseEntity.getId(), courseEntity.getUid(), courseEntity.getTitle(), tagDto);
    }

    public void removeLearningTagFromCourse(Long courseId, Long learningTagId) {
        log.info("Removing learning tag {} from course {}", learningTagId, courseId);
        CourseLearningTagId courseLearningTagId = new CourseLearningTagId(learningTagId, courseId);
        if (!courseTagRepository.existsById(courseLearningTagId)) {
            throw new ResourceNotFoundException(String.format("Association between course %s and learning tag %s not found", courseId, learningTagId));
        }
        courseTagRepository.deleteById(courseLearningTagId);
        log.info("Removed learning tag {} from course {}", learningTagId, courseId);
    }
}
