package uk.gov.cslearning.catalogue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.api.models.SimplePage;
import uk.gov.cslearning.catalogue.domain.LearningTag;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import javax.validation.Valid;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LearningTagService {

    private final ILearningTagRepository learningTagRepository;
    private final LearningTagFactory learningTagFactory;

    public LearningTagService(ILearningTagRepository learningTagRepository, LearningTagFactory learningTagDtoFactory) {
        this.learningTagRepository = learningTagRepository;
        this.learningTagFactory = learningTagDtoFactory;
    }

    public SimplePage<LearningTagDto> getLearningTags(Pageable pageable) {
        Page<LearningTag> page = learningTagRepository.findAll(pageable);
        return new SimplePage<>(page.getContent().stream().map(learningTagFactory::createDto).collect(Collectors.toList()),
                page.getTotalElements(), pageable);
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
                .map(learningTag -> Optional.ofNullable(learningTagDto.getParentId())
                        .map(parentId -> {
                            LearningTag parent = learningTagRepository.findById(learningTagDto.getParentId())
                                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Parent learning tag with ID %s not found", learningTagDto.getParentId())));
                            return learningTagFactory.update(learningTag, learningTagDto, parent);
                        }).orElse(learningTagFactory.update(learningTag, learningTagDto)))
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Learning tag with ID %s not found", learningTagId)));
        learningTagRepository.save(tag);
        return learningTagFactory.createDto(tag);
    }
}
