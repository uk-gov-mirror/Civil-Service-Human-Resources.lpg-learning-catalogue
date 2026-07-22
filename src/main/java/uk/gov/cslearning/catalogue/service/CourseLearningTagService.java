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

    public LearningTagDto addTagToCourse(Long courseId, LearningTagDto learningTagDto) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Course with ID %s not found", courseId)));

        LearningTag learningTag = learningTagRepository.findById(learningTagDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Learning tag with ID %s not found", learningTagDto.getId())));

        CourseTagEntity courseTag = new CourseTagEntity(learningTag, course);
        courseTagRepository.save(courseTag);

        return learningTagFactory.createDto(learningTag);
    }

    public void removeTagFromCourse(Long courseId, Long tagId) {
        CourseTagId courseTagId = new CourseTagId(tagId, courseId);
        if (!courseTagRepository.existsById(courseTagId)) {
            throw new ResourceNotFoundException(String.format("Association between course %s and tag %s not found", courseId, tagId));
        }
        courseTagRepository.deleteById(courseTagId);
    }
}
