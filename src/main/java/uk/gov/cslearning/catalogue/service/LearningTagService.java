package uk.gov.cslearning.catalogue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.api.models.CourseLearningTagResponse;
import uk.gov.cslearning.catalogue.api.models.SimplePage;
import uk.gov.cslearning.catalogue.domain.*;
import uk.gov.cslearning.catalogue.dto.BulkUpdateDto;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LearningTagService {

    private final ILearningTagRepository learningTagRepository;
    private final ICourseTagRepository courseTagRepository;
    private final LearningTagFactory learningTagFactory;

    public LearningTagService(ILearningTagRepository learningTagRepository, ICourseTagRepository courseTagRepository, LearningTagFactory learningTagDtoFactory) {
        this.learningTagRepository = learningTagRepository;
        this.courseTagRepository = courseTagRepository;
        this.learningTagFactory = learningTagDtoFactory;
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
}
