package be.he2b.don5.search.infrastructure.elasticsearch;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, String>, UserSearchRepositoryCustom {
    List<UserSearchDocument> findByNameContainingIgnoreCaseOrBioContainingIgnoreCase(String name, String bio);
    List<UserSearchDocument> findByInterestsIn(List<String> interests);
}
