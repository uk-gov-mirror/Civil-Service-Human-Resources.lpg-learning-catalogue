package uk.gov.cslearning.catalogue.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.api.models.*;
import uk.gov.cslearning.catalogue.domain.*;
import uk.gov.cslearning.catalogue.dto.BulkUpdateDto;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseStatusRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LearningTagService {

    private final ILearningTagRepository learningTagRepository;
    private final ICourseTagRepository courseTagRepository;
    private final ICourseRepository courseRepository;
    private final ICourseStatusRepository courseStatusRepository;
    private final LearningTagFactory learningTagFactory;
    private final CourseRepository elasticCourseRepository;

    public LearningTagService(ILearningTagRepository learningTagRepository, ICourseTagRepository courseTagRepository,
                              ICourseRepository courseRepository, ICourseStatusRepository courseStatusRepository,
                              LearningTagFactory learningTagDtoFactory, CourseRepository elasticCourseRepository) {
        this.learningTagRepository = learningTagRepository;
        this.courseTagRepository = courseTagRepository;
        this.courseRepository = courseRepository;
        this.courseStatusRepository = courseStatusRepository;
        this.learningTagFactory = learningTagDtoFactory;
        this.elasticCourseRepository = elasticCourseRepository;
    }

    public SimplePage<LearningTagDto> getLearningTags(Pageable pageable) {
        Page<LearningTag> page = learningTagRepository.findAll(pageable);
        return new SimplePage<>(page.getContent().stream().map(learningTagFactory::createDto).collect(Collectors.toList()),
                page.getTotalElements(), pageable);
    }

    public SimplePage<CourseLearningTagResponse> getCoursesByLearningTagId(Long learningTagId, Pageable pageable) {
        Page<CourseLearningTagEntity> page = courseTagRepository.findByLearningTagIdOrderByCourseTitleAsc(learningTagId, pageable);
        return new SimplePage<>(
                page.getContent().stream()
                        .map(entity -> new CourseLearningTagResponse(
                                entity.getCourse().getUid(),
                                entity.getCourse().getTitle(),
                                entity.getCourse().getStatus().getName()))
                        .collect(Collectors.toList()),
                page.getTotalElements(),
                pageable);
    }

    public LearningTagDto createLearningTag(@Valid LearningTagDto learningTagDto) {
        LearningTag newTag = Optional.ofNullable(learningTagDto.getParentId())
                .map(parentId -> {
                    LearningTag parent = learningTagRepository.findById(learningTagDto.getParentId())
                            .orElseThrow(() -> new ResourceNotFoundException(String.format("Parent learning tag with ID %s not found", learningTagDto.getParentId())));
                    return learningTagFactory.create(learningTagDto, parent);
                }).orElse(learningTagFactory.create(learningTagDto));
        learningTagRepository.save(newTag);
        return learningTagFactory.createDto(newTag);
    }

    public LearningTagDto updateLearningTag(Long learningTagId, @Valid LearningTagDto learningTagDto) {
        LearningTag tag = learningTagRepository.findById(learningTagId)
                .map(learningTag -> {
                    if (learningTagDto.getParentId() == null) {
                        return learningTagFactory.update(learningTag, learningTagDto);
                    } else {
                        LearningTag parent = learningTagRepository.findById(learningTagDto.getParentId())
                                .orElseThrow(() -> new ResourceNotFoundException(String.format("Parent learning tag with ID %s not found", learningTagDto.getParentId())));
                        return learningTagFactory.update(learningTag, learningTagDto, parent);
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Learning tag with ID %s not found", learningTagId)));
        learningTagRepository.save(tag);
        return learningTagFactory.createDto(tag);
    }

    public BulkUpdateDto updateLearningTagState(@Valid LearningTagBulkStateDto dto) {
        List<LearningTag> tags = learningTagRepository.findAllById(dto.getIds());
        List<Long> successful = new ArrayList<>();
        List<Long> failed = new ArrayList<>();
        learningTagFactory.updateState(tags, dto.getState())
                .forEach(lt -> {
                    try {
                        learningTagRepository.save(lt);
                        successful.add(lt.getId());
                    } catch (Exception e) {
                        failed.add(lt.getId());
                    }
                });
        return new BulkUpdateDto(successful, failed);
    }

    public LearningTag getLearningTagById(Long learningTagId) {
        return learningTagRepository.findById(learningTagId)
                .orElseThrow(() -> {
                    log.error("Learning tag with ID {} not found", learningTagId);
                    return new ResourceNotFoundException(String.format("Learning tag with ID %s not found", learningTagId));
                });
    }

    public CourseBulkUpdateResponse removeCoursesFromLearningTag(Long learningTagId, CourseIdsDto courseIdsDto) {
        LearningTag learningTag = getLearningTagById(learningTagId);
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        courseIdsDto.getIds().forEach(courseUid -> {
            try {
                CourseEntity course = courseRepository.findByUid(courseUid)
                        .orElseThrow(() -> {
                            log.error("Course with UID {} not found", courseUid);
                            return new ResourceNotFoundException(String.format("Course with UID %s not found", courseUid));
                        });
                CourseLearningTagId id = new CourseLearningTagId(learningTag.getId(), course.getId());
                if (courseTagRepository.existsById(id)) {
                    courseTagRepository.deleteById(id);
                    successful.add(courseUid);
                } else {
                    log.error("Course with UID {} is not linked with the Learning tag with ID {}", courseUid, learningTagId);
                    throw new ResourceNotFoundException(String.format("Course with UID %s is not linked with the Learning tag with ID %s", courseUid, learningTagId));
                }
            } catch (Exception e) {
                failed.add(courseUid);
            }
        });

        return new CourseBulkUpdateResponse(successful, failed);
    }

    public void assignCoursesToTag(LearningTagCourseBulkRequest request) {
        List<LearningTag> learningTags = learningTagRepository.findAllById(request.getLearningTagIds());

        request.getCourseIds().forEach(courseUid -> {
            CourseEntity course = courseRepository.findByUid(courseUid)
                    .orElseGet(() -> {
                        Optional<Course> elasticCourseOptional = elasticCourseRepository.findById(courseUid);

                        if (!elasticCourseOptional.isPresent()) {
                            log.error("Course with UID {} not found in ElasticSearch, skipping", courseUid);
                            return null;
                        }

                        Course elasticCourse = elasticCourseOptional.get();

                        CourseStatusEntity status = courseStatusRepository.findByName(elasticCourse.getStatus().getValue())
                                .orElseGet(() -> courseStatusRepository.save(new CourseStatusEntity(elasticCourse.getStatus().getValue())));

                        return courseRepository.save(new CourseEntity(elasticCourse.getId(), elasticCourse.getTitle(), status));
                    });

            if (course != null) {
                learningTags.forEach(learningTag -> {
                    if (!courseTagRepository.findByLearningTagIdAndCourseId(learningTag.getId(), course.getId()).isPresent()) {
                        courseTagRepository.save(new CourseLearningTagEntity(learningTag, course));
                    }
                });
            }
        });
    }
}
