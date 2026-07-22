package be.he2b.don5.search.infrastructure.elasticsearch.repository.meeting;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;

import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;
import lombok.AllArgsConstructor;

/**
 * Implementation of custom meeting search queries.
 *
 * <p>Builds Elasticsearch JSON queries (fuzzy matching) and executes them
 * using {@link ElasticsearchOperations}.
 */
@Component
@AllArgsConstructor
public class MeetingSearchRepositoryCustomImpl implements MeetingSearchRepositoryCustom {
    
    /**
     * Low-level Elasticsearch operations used to run queries.
     */
    private final ElasticsearchOperations elasticsearchOperations;


    /**
     * {@inheritDoc}
     */
    @Override
    public List<MeetingSearchDocument> searchByLocationOrInterestsFuzzy(String query) {
        String elasticsearchQuery = "{\n" +
            "  \"multi_match\": {\n" +
            "    \"query\": \"" + query + "\",\n" +
            "    \"fields\": [\"location^2\", \"interests\"],\n" +
            "    \"fuzziness\": \"AUTO\"\n" +
            "  }\n" +
            "}";

        SearchHits<MeetingSearchDocument> hits = 
                elasticsearchOperations.search(new StringQuery(elasticsearchQuery), MeetingSearchDocument.class);
        
        return hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MeetingSearchDocument> searchByStatusAndLocationOrInterestsFuzzy(String status, String query) {
        String elasticsearchQuery = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      { \"term\": { \"status\": \"" + status.toLowerCase() + "\" } },\n" +
            "      { \"multi_match\": {\n" +
            "          \"query\": \"" + query + "\",\n" +
            "          \"fields\": [\"location^2\", \"interests\"],\n" +
            "          \"fuzziness\": \"AUTO\"\n" +
            "      }}\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        SearchHits<MeetingSearchDocument> hits = 
                elasticsearchOperations.search(new StringQuery(elasticsearchQuery), MeetingSearchDocument.class);
        
        return hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .collect(Collectors.toList());
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MeetingSearchDocument> searchByUserIdAndStatusAndLocationOrInterestsFuzzy(
            String userId, String status, String query
    ) {
        String elasticsearchQuery =
            "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      { \"term\": { \"status\": \"" + status.toLowerCase() + "\" } },\n" +
            "      { \"bool\": {\n" +
            "          \"should\": [\n" +
            "            { \"term\": { \"organizer\": \"" + userId + "\" } },\n" +
            "            { \"term\": { \"participants\": \"" + userId + "\" } }\n" +
            "          ],\n" +
            "          \"minimum_should_match\": 1\n" +
            "      }},\n" +
            "      { \"multi_match\": {\n" +
            "          \"query\": \"" + query + "\",\n" +
            "          \"fields\": [\"location^2\", \"interests\"],\n" +
            "          \"fuzziness\": \"AUTO\"\n" +
            "      }}\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        SearchHits<MeetingSearchDocument> hits =
            elasticsearchOperations.search(new StringQuery(elasticsearchQuery), MeetingSearchDocument.class);

        return hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .collect(Collectors.toList());
    }
}
