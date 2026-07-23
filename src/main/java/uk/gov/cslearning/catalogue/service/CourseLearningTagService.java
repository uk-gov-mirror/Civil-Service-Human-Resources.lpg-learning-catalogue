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

    public CourseLearningTagService(ICourseRepository courseRepository, ILearningTagRepository learningTagRepository, ICourseTagRepository courseTagRepository) {
        this.courseRepository = courseRepository;
        this.learningTagRepository = learningTagRepository;
        this.courseTagRepository = courseTagRepository;
    }

    public void removeLearningTagFromCourse(String courseUid, String learningTagCode) {
        log.info("Removing learning tag {} from course {}", learningTagCode, courseUid);
        CourseEntity courseEntity = courseRepository.findByUid(courseUid)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Course with UID %s not found", courseUid)));

        LearningTag learningTag = learningTagRepository.findByCode(learningTagCode)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Learning tag with code %s not found", learningTagCode)));

        CourseLearningTagId courseLearningTagId = new CourseLearningTagId(learningTag.getId(), courseEntity.getId());
        if (!courseTagRepository.existsById(courseLearningTagId)) {
            throw new ResourceNotFoundException(String.format("Association between course %s and learning tag %s not found", courseUid, learningTagCode));
        }
        courseTagRepository.deleteById(courseLearningTagId);
        log.info("Removed learning tag {} from course {}", learningTagCode, courseUid);
    }
}
