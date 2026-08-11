package uk.gov.cslearning.catalogue.repository.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.domain.CourseStatusEntity;

import java.util.Optional;

@Repository
public interface ICourseStatusRepository extends JpaRepository<CourseStatusEntity, Long> {
    Optional<CourseStatusEntity> findByName(String name);
}
