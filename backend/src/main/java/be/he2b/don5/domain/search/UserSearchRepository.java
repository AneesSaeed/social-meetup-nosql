package be.he2b.don5.domain.search;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, String> {
    List<UserSearchDocument> findByNameContainingIgnoreCaseOrBioContainingIgnoreCase(String name, String bio);
    List<UserSearchDocument> findByInterestsIn(List<String> interests);
}

