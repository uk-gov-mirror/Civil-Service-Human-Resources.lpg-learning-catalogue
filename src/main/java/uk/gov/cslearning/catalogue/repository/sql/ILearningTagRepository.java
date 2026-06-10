package uk.gov.cslearning.catalogue.repository.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.domain.LearningTag;

@Repository
public interface ILearningTagRepository extends JpaRepository<LearningTag, Long> {
}
