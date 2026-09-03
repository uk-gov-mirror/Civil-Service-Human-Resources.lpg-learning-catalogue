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
import uk.gov.cslearning.catalogue.api.models.BulkUpdateResponse;
import uk.gov.cslearning.catalogue.api.models.IdsDto;
import uk.gov.cslearning.catalogue.api.models.SimplePage;
import uk.gov.cslearning.catalogue.domain.CourseEntity;
import uk.gov.cslearning.catalogue.domain.CourseLearningTagId;
import uk.gov.cslearning.catalogue.domain.LearningTag;
import uk.gov.cslearning.catalogue.domain.LearningTagHyperlink;
import uk.gov.cslearning.catalogue.domain.LearningTagHyperlinkDto;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseStatusRepository;
import uk.gov.cslearning.catalogue.repository.sql.ICourseTagRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagHyperlinkRepository;
import uk.gov.cslearning.catalogue.repository.sql.ILearningTagRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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

    @Test
    public void testCreateLearningTagHyperlink() {
        Long tagId = 1L;
        LearningTag tag = new LearningTag();
        tag.setId(tagId);

        LearningTagHyperlinkDto inputDto = new LearningTagHyperlinkDto(null, "BBC", "BBC Desc", "https://bbc.co.uk");
        LearningTagHyperlink createdEntity = new LearningTagHyperlink(100L, tag, "https://bbc.co.uk", "BBC", "BBC Desc", null, null);
        LearningTagHyperlinkDto expectedResultDto = new LearningTagHyperlinkDto(100L, "BBC", "BBC Desc", "https://bbc.co.uk");

        when(learningTagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(learningTagFactory.createHyperlink(inputDto, tag)).thenReturn(createdEntity);
        when(learningTagHyperlinkRepository.save(createdEntity)).thenReturn(createdEntity);
        when(learningTagFactory.createHyperlinkDto(createdEntity)).thenReturn(expectedResultDto);

        LearningTagHyperlinkDto result = learningTagService.createLearningTagHyperlink(tagId, inputDto);

        assertEquals(Long.valueOf(100L), result.getId());
        assertEquals("BBC", result.getTitle());
        assertEquals("BBC Desc", result.getDescription());
        assertEquals("https://bbc.co.uk", result.getHref());

        verify(learningTagRepository).findById(tagId);
        verify(learningTagFactory).createHyperlink(inputDto, tag);
        verify(learningTagHyperlinkRepository).save(createdEntity);
        verify(learningTagFactory).createHyperlinkDto(createdEntity);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testCreateLearningTagHyperlinkWhenTagNotFound() {
        Long tagId = 999L;
        LearningTagHyperlinkDto inputDto = new LearningTagHyperlinkDto(null, "BBC", "BBC Desc", "https://bbc.co.uk");

        when(learningTagRepository.findById(tagId)).thenReturn(Optional.empty());

        learningTagService.createLearningTagHyperlink(tagId, inputDto);
    }

    @Test
    public void testRemoveHyperlinksFromLearningTag() {
        Long tagId = 1L;
        LearningTag tag = new LearningTag();
        tag.setId(tagId);

        when(learningTagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        LearningTagHyperlink hyperlink1 = new LearningTagHyperlink(101L, tag, "https://bbc.co.uk", "BBC", "BBC Desc", null, null);
        LearningTagHyperlink hyperlink2 = new LearningTagHyperlink(102L, tag, "https://sky.com", "Sky", "Sky Desc", null, null);

        when(learningTagHyperlinkRepository.findByIdAndLearningTagId(101L, tagId)).thenReturn(Optional.of(hyperlink1));
        when(learningTagHyperlinkRepository.findByIdAndLearningTagId(102L, tagId)).thenReturn(Optional.of(hyperlink2));
        when(learningTagHyperlinkRepository.findByIdAndLearningTagId(103L, tagId)).thenReturn(Optional.empty());

        IdsDto<Long> dto = new IdsDto<>(Arrays.asList(101L, 102L, 103L));
        BulkUpdateResponse<Long> response = learningTagService.removeHyperlinksFromLearningTag(tagId, dto);

        assertEquals(2, response.getSuccessfulIds().size());
        assertTrue(response.getSuccessfulIds().contains(101L));
        assertTrue(response.getSuccessfulIds().contains(102L));
        assertEquals(1, response.getFailedIds().size());
        assertTrue(response.getFailedIds().contains(103L));

        verify(learningTagHyperlinkRepository).delete(hyperlink1);
        verify(learningTagHyperlinkRepository).delete(hyperlink2);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testRemoveHyperlinksFromLearningTagWhenTagNotFound() {
        Long tagId = 999L;
        when(learningTagRepository.findById(tagId)).thenReturn(Optional.empty());

        IdsDto<Long> dto = new IdsDto<>(Arrays.asList(101L));
        learningTagService.removeHyperlinksFromLearningTag(tagId, dto);
    }

    @Test
    public void testRemoveCoursesFromLearningTag() {
        Long tagId = 1L;
        LearningTag tag = new LearningTag();
        tag.setId(tagId);

        when(learningTagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        CourseEntity course1 = new CourseEntity();
        course1.setId(10L);
        course1.setUid("course-1");

        CourseEntity course2 = new CourseEntity();
        course2.setId(20L);
        course2.setUid("course-2");

        when(courseRepository.findByUid("course-1")).thenReturn(Optional.of(course1));
        when(courseRepository.findByUid("course-2")).thenReturn(Optional.of(course2));
        when(courseRepository.findByUid("course-3")).thenReturn(Optional.empty());

        when(courseTagRepository.existsById(new CourseLearningTagId(tagId, 10L))).thenReturn(true);
        when(courseTagRepository.existsById(new CourseLearningTagId(tagId, 20L))).thenReturn(false);

        IdsDto<String> dto = new IdsDto<>(Arrays.asList("course-1", "course-2", "course-3"));
        BulkUpdateResponse<String> response = learningTagService.removeCoursesFromLearningTag(tagId, dto);

        assertEquals(1, response.getSuccessfulIds().size());
        assertTrue(response.getSuccessfulIds().contains("course-1"));
        assertEquals(2, response.getFailedIds().size());
        assertTrue(response.getFailedIds().contains("course-2"));
        assertTrue(response.getFailedIds().contains("course-3"));

        verify(courseTagRepository).deleteById(new CourseLearningTagId(tagId, 10L));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testRemoveCoursesFromLearningTagWhenTagNotFound() {
        Long tagId = 999L;
        when(learningTagRepository.findById(tagId)).thenReturn(Optional.empty());

        IdsDto<String> dto = new IdsDto<>(Arrays.asList("course-1"));
        learningTagService.removeCoursesFromLearningTag(tagId, dto);
    }
}
