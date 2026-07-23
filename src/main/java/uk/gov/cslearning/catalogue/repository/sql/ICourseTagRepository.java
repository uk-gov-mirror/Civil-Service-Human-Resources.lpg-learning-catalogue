package uk.gov.cslearning.catalogue.repository.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.domain.CourseLearningTagEntity;
import uk.gov.cslearning.catalogue.domain.CourseLearningTagId;

@Repository
public interface ICourseTagRepository extends JpaRepository<CourseLearningTagEntity, CourseLearningTagId> {
    Page<CourseLearningTagEntity> findByLearningTagIdOrderByCourseTitleAsc(Long learningTagId, Pageable pageable);
}
