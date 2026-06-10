package uk.gov.cslearning.catalogue.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.api.models.SimplePage;
import uk.gov.cslearning.catalogue.domain.LearningTag;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import java.util.stream.Collectors;

@Service
public class LearningTagService {

    private final ILearningTagRepository learningTagRepository;
    private final LearningTagDtoFactory learningTagDtoFactory;

    public LearningTagService(ILearningTagRepository learningTagRepository, LearningTagDtoFactory learningTagDtoFactory) {
        this.learningTagRepository = learningTagRepository;
        this.learningTagDtoFactory = learningTagDtoFactory;
    }

    public SimplePage<LearningTagDto> getLearningTags(Pageable pageable) {
        Page<LearningTag> page = learningTagRepository.findAll(pageable);
        return new SimplePage<>(page.getContent().stream().map(learningTagDtoFactory::create).collect(Collectors.toList()),
                page.getTotalElements(), pageable);
    }

}
