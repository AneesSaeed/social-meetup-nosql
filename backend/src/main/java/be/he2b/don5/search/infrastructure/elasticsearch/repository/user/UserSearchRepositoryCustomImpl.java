package be.he2b.don5.search.infrastructure.elasticsearch.repository.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;

import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;
import lombok.AllArgsConstructor;

/**
 * Implementation of custom user search queries.
 *
 * <p>Builds Elasticsearch JSON queries (fuzzy matching) and executes them
 * using {@link ElasticsearchOperations}.
 */
@Component
@AllArgsConstructor
public class UserSearchRepositoryCustomImpl implements UserSearchRepositoryCustom {
    
    /**
     * Low-level Elasticsearch operations used to run queries.
     */    
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserSearchDocument> searchByNameOrBioFuzzy(String query) {
        String elasticsearchQuery = "{\n" +
            "  \"multi_match\": {\n" +
            "    \"query\": \"" + query + "\",\n" +
            "    \"fields\": [\"name^2\", \"bio\", \"interests\"],\n" +
            "    \"fuzziness\": \"AUTO\"\n" +
            "  }\n" +
            "}";
        
        StringQuery stringQuery = new StringQuery(elasticsearchQuery);
        SearchHits<UserSearchDocument> hits = 
                elasticsearchOperations.search(stringQuery, UserSearchDocument.class);
        
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    /**
     * Searches users by interests (OR condition, fuzzy).
     *
     * @param interests list of interests
     * @return matching users
     */
    @Override
    public List<UserSearchDocument> searchByInterestsAnyFuzzy(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return List.of();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"bool\": {\n    \"should\": [\n");
        for (int i = 0; i < interests.size(); i++) {
            String term = interests.get(i);
            sb.append("      { \"match\": { \"interests\": { \"query\": \"")
              .append(term)
              .append("\", \"fuzziness\": \"AUTO\" } } }");
            if (i < interests.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
        }
        sb.append("    ],\n    \"minimum_should_match\": 1\n  }\n}");

        StringQuery stringQuery = new StringQuery(sb.toString());
        SearchHits<UserSearchDocument> hits = 
            elasticsearchOperations.search(stringQuery, UserSearchDocument.class);
        
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

    /**
     * Searches users by interests (AND condition, fuzzy).
     *
     * @param interests list of interests
     * @return matching users
     */
    @Override
    public List<UserSearchDocument> searchByInterestsAllFuzzy(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return List.of();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"bool\": {\n    \"must\": [\n");
        for (int i = 0; i < interests.size(); i++) {
            String term = interests.get(i);
            sb.append("      { \"match\": { \"interests\": { \"query\": \"")
              .append(term)
              .append("\", \"fuzziness\": \"AUTO\" } } }");
            if (i < interests.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
        }
        sb.append("    ]\n  }\n}");

        StringQuery stringQuery = new StringQuery(sb.toString());
        SearchHits<UserSearchDocument> hits = elasticsearchOperations.search(stringQuery, UserSearchDocument.class);
        return hits.getSearchHits().stream().map(hit -> hit.getContent()).collect(Collectors.toList());
    }
}

