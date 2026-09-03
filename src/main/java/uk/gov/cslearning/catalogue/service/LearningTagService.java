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
import uk.gov.cslearning.catalogue.repository.sql.*;

import javax.validation.Valid;
import java.util.*;
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
    private final ILearningTagHyperlinkRepository learningTagHyperlinkRepository;

    public LearningTagService(ILearningTagRepository learningTagRepository, ICourseTagRepository courseTagRepository,
                              ICourseRepository courseRepository, ICourseStatusRepository courseStatusRepository,
                              LearningTagFactory learningTagDtoFactory, CourseRepository elasticCourseRepository,
                              ILearningTagHyperlinkRepository learningTagHyperlinkRepository) {
        this.learningTagRepository = learningTagRepository;
        this.courseTagRepository = courseTagRepository;
        this.courseRepository = courseRepository;
        this.courseStatusRepository = courseStatusRepository;
        this.learningTagFactory = learningTagDtoFactory;
        this.elasticCourseRepository = elasticCourseRepository;
        this.learningTagHyperlinkRepository = learningTagHyperlinkRepository;
    }

    public SimplePage<LearningTagHyperlinkDto> getHyperlinksByLearningTagId(Long learningTagId, Pageable pageable) {
        Page<LearningTagHyperlink> page = learningTagHyperlinkRepository.findByLearningTagIdOrderByTitleAsc(learningTagId, pageable);
        return new SimplePage<>(
                page.getContent().stream()
                        .map(learningTagFactory::createHyperlinkDto)
                        .collect(Collectors.toList()),
                page.getTotalElements(),
                pageable);
    }

    public LearningTagHyperlinkDto createLearningTagHyperlink(Long learningTagId, @Valid LearningTagHyperlinkDto dto) {
        LearningTag learningTag = getLearningTagById(learningTagId);
        LearningTagHyperlink hyperlink = learningTagFactory.createHyperlink(dto, learningTag);
        learningTagHyperlinkRepository.save(hyperlink);
        return learningTagFactory.createHyperlinkDto(hyperlink);
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
                                entity.getCourse().getShortDescription(),
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

    public BulkUpdateResponse<String> removeCoursesFromLearningTag(Long learningTagId, IdsDto<String> courseIdsDto) {
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

        return new BulkUpdateResponse<>(successful, failed);
    }

    public BulkUpdateResponse<Long> removeHyperlinksFromLearningTag(Long learningTagId, IdsDto<Long> hyperlinkIdsDto) {
        LearningTag learningTag = getLearningTagById(learningTagId);
        List<Long> successful = new ArrayList<>();
        List<Long> failed = new ArrayList<>();

        if (hyperlinkIdsDto != null && hyperlinkIdsDto.getIds() != null) {
            hyperlinkIdsDto.getIds().forEach(hyperlinkId -> {
                try {
                    LearningTagHyperlink hyperlink = learningTagHyperlinkRepository.findByIdAndLearningTagId(hyperlinkId, learningTag.getId())
                            .orElseThrow(() -> {
                                log.error("Hyperlink with ID {} not found for Learning tag with ID {}", hyperlinkId, learningTagId);
                                return new ResourceNotFoundException(String.format("Hyperlink with ID %s not found for Learning tag with ID %s", hyperlinkId, learningTagId));
                            });
                    learningTagHyperlinkRepository.delete(hyperlink);
                    successful.add(hyperlinkId);
                } catch (Exception e) {
                    failed.add(hyperlinkId);
                }
            });
        }

        return new BulkUpdateResponse<>(successful, failed);
    }

    public BulkUpdateResponse<LearningTagCourseUpdateResponse> assignCoursesToTag(LearningTagCourseBulkRequest request) {
        Map<Long, Collection<Long>> learningTagsToCoursesMap = new HashMap<>();
        List<LearningTag> learningTags = learningTagRepository.findAllById(request.getLearningTagIds());

        request.getCourseIds().forEach(courseUid -> {
            CourseEntity course = courseRepository.findByUid(courseUid)
                    .orElseGet(() -> {
                        Optional<Course> elasticCourseOptional = elasticCourseRepository.findById(courseUid);

                        if (!elasticCourseOptional.isPresent()) {
                            log.error("Course with UID {} not found in ElasticSearch. Skipping", courseUid);
                            return null;
                        }

                        Course elasticCourse = elasticCourseOptional.get();

                        CourseStatusEntity status = courseStatusRepository.findByName(elasticCourse.getStatus().getValue())
                                .orElseGet(() -> courseStatusRepository.save(new CourseStatusEntity(elasticCourse.getStatus().getValue())));

                        return courseRepository.save(new CourseEntity(elasticCourse.getId(), elasticCourse.getTitle(), elasticCourse.getShortDescription(), status));
                    });

            if (course == null) {
                return;
            }
            learningTags.forEach(learningTag -> {
                if (courseTagRepository.findByLearningTagIdAndCourseId(learningTag.getId(), course.getId()).isPresent()) {
                    log.info("CourseId {} is already assigned to learningTagId {}. Skipping", course.getId(), learningTag.getId());
                    return;
                }
                courseTagRepository.save(new CourseLearningTagEntity(learningTag, course));
                learningTagsToCoursesMap.computeIfAbsent(learningTag.getId(), k -> new ArrayList<>()).add(course.getId());
            });
        });
        Collection<LearningTagCourseUpdateResponse> successfulUpdates = learningTagsToCoursesMap
                .entrySet().stream().map(longCollectionEntry -> new LearningTagCourseUpdateResponse(longCollectionEntry.getValue(), Collections.emptyList(), longCollectionEntry.getKey())).collect(Collectors.toList());
        return new BulkUpdateResponse<>(successfulUpdates, Collections.emptyList());
    }
}
