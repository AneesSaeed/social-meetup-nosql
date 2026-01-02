package be.he2b.don5.search.infrastructure.elasticsearch.repository.meeting;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;

/**
 * Elasticsearch repository for {@link MeetingSearchDocument}.
 *
 * <p>Provides basic index operations and custom fuzzy search queries.</p>
 */
public interface MeetingSearchRepository 
        extends ElasticsearchRepository<MeetingSearchDocument, String>, MeetingSearchRepositoryCustom {}
