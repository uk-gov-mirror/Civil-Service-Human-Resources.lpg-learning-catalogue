package uk.gov.cslearning.catalogue.repository.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.domain.LearningTag;

import java.util.Optional;

@Repository
public interface ILearningTagRepository extends JpaRepository<LearningTag, Long> {
    Optional<LearningTag> findByCode(String code);
}
