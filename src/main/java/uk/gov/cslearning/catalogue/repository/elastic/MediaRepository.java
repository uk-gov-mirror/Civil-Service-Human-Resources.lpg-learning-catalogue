package uk.gov.cslearning.catalogue.repository.elastic;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.domain.Media;

@Repository
public interface MediaRepository extends ElasticsearchRepository<Media, String> {
}
