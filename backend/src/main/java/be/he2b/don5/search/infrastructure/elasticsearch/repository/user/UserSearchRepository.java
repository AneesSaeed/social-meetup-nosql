package be.he2b.don5.search.infrastructure.elasticsearch.repository.user;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;

/**
 * Elasticsearch repository for {@link UserSearchDocument}.
 *
 * <p>Provides basic index operations and custom fuzzy search queries.
 */
public interface UserSearchRepository 
        extends ElasticsearchRepository<UserSearchDocument, String>, UserSearchRepositoryCustom {}
