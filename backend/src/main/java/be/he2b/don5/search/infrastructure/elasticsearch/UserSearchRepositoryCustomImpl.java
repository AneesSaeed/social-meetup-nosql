package be.he2b.don5.search.infrastructure.elasticsearch;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UserSearchRepositoryCustomImpl implements UserSearchRepositoryCustom {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    @Override
    public List<UserSearchDocument> searchByNameOrBioFuzzy(String query) {
        // Fuzzy multi-match query with AUTO fuzziness (1-2 edits depending on term length)
        String elasticsearchQuery = "{\n" +
            "  \"multi_match\": {\n" +
            "    \"query\": \"" + query + "\",\n" +
            "    \"fields\": [\"name^2\", \"bio\", \"interests\"],\n" +
            "    \"fuzziness\": \"AUTO\"\n" +
            "  }\n" +
            "}";
        
        StringQuery stringQuery = new StringQuery(elasticsearchQuery);
        SearchHits<UserSearchDocument> hits = elasticsearchOperations.search(stringQuery, UserSearchDocument.class);
        
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

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
        SearchHits<UserSearchDocument> hits = elasticsearchOperations.search(stringQuery, UserSearchDocument.class);
        return hits.getSearchHits().stream().map(hit -> hit.getContent()).collect(Collectors.toList());
    }

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

