package uk.gov.cslearning.catalogue.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.cslearning.catalogue.api.models.SimplePage;
import uk.gov.cslearning.catalogue.domain.LearningTag;
import uk.gov.cslearning.catalogue.domain.LearningTagHyperlink;
import uk.gov.cslearning.catalogue.domain.LearningTagHyperlinkDto;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseStatusRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagHyperlinkRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LearningTagServiceTest {

    @Mock
    private ILearningTagRepository learningTagRepository;

    @Mock
    private ICourseTagRepository courseTagRepository;

    @Mock
    private ICourseRepository courseRepository;

    @Mock
    private ICourseStatusRepository courseStatusRepository;

    @Mock
    private LearningTagFactory learningTagFactory;

    @Mock
    private CourseRepository elasticCourseRepository;

    @Mock
    private ILearningTagHyperlinkRepository learningTagHyperlinkRepository;

    private LearningTagService learningTagService;

    @Before
    public void setUp() {
        learningTagService = new LearningTagService(
                learningTagRepository,
                courseTagRepository,
                courseRepository,
                courseStatusRepository,
                learningTagFactory,
                elasticCourseRepository,
                learningTagHyperlinkRepository
        );
    }

    @Test
    public void testGetHyperlinksByLearningTagId() {
        Long tagId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        LearningTag tag = new LearningTag();
        tag.setId(tagId);

        LearningTagHyperlink hyperlink = new LearningTagHyperlink(100L, tag, "https://bbc.co.uk", "BBC", "BBC Desc", null, null);
        List<LearningTagHyperlink> hyperlinks = Collections.singletonList(hyperlink);
        Page<LearningTagHyperlink> page = new PageImpl<>(hyperlinks, pageable, 1);

        LearningTagHyperlinkDto dto = new LearningTagHyperlinkDto(100L, "BBC", "BBC Desc", "https://bbc.co.uk");

        when(learningTagHyperlinkRepository.findByLearningTagIdOrderByTitleAsc(tagId, pageable)).thenReturn(page);
        when(learningTagFactory.createHyperlinkDto(hyperlink)).thenReturn(dto);

        SimplePage<LearningTagHyperlinkDto> result = learningTagService.getHyperlinksByLearningTagId(tagId, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(Long.valueOf(100L), result.getContent().get(0).getId());
        assertEquals("BBC", result.getContent().get(0).getTitle());
        assertEquals("BBC Desc", result.getContent().get(0).getDescription());
        assertEquals("https://bbc.co.uk", result.getContent().get(0).getHref());
        assertEquals(1, result.getTotalResults());

        verify(learningTagHyperlinkRepository).findByLearningTagIdOrderByTitleAsc(tagId, pageable);
        verify(learningTagFactory).createHyperlinkDto(hyperlink);
    }
}
