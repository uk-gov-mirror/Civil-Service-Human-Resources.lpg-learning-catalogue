package uk.gov.cslearning.catalogue.repository.sql;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.domain.LearningTagHyperlink;

import java.util.Optional;

@Repository
public interface ILearningTagHyperlinkRepository extends JpaRepository<LearningTagHyperlink, Long> {
    Page<LearningTagHyperlink> findByLearningTagIdOrderByTitleAsc(Long learningTagId, Pageable pageable);

    Optional<LearningTagHyperlink> findByIdAndLearningTagId(Long id, Long learningTagId);
}
