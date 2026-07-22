package uk.gov.cslearning.catalogue.service;

import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.*;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

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

    public CourseDto addTagToCourse(Long courseId, LearningTagDto learningTagDto) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Course with ID %s not found", courseId)));

        LearningTag learningTag = learningTagRepository.findById(learningTagDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Learning tag with ID %s not found", learningTagDto.getId())));

        CourseTagEntity courseTag = new CourseTagEntity(learningTag, course);
        courseTagRepository.save(courseTag);

        LearningTagDto tagDto = learningTagFactory.createDto(learningTag);
        return new CourseDto(course.getId(), course.getUid(), course.getTitle(), tagDto);
    }

    public void removeTagFromCourse(Long courseId, Long learningTagId) {
        CourseLearningTagId courseLearningTagId = new CourseLearningTagId(learningTagId, courseId);
        if (!courseTagRepository.existsById(courseLearningTagId)) {
            throw new ResourceNotFoundException(String.format("Association between course %s and learning tag %s not found", courseId, learningTagId));
        }
        courseTagRepository.deleteById(courseLearningTagId);
    }
}
