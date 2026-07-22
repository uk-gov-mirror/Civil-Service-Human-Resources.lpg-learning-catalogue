package uk.gov.cslearning.catalogue.repository.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.domain.CourseTagEntity;
import uk.gov.cslearning.catalogue.domain.CourseLearningTagId;

@Repository
public interface ICourseTagRepository extends JpaRepository<CourseTagEntity, CourseLearningTagId> {
}
