package be.he2b.don5.domain.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MeetingSearchRepository extends ElasticsearchRepository<MeetingSearchDocument, String>, MeetingSearchRepositoryCustom {
}
