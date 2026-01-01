package be.he2b.don5.search.infrastructure.elasticsearch;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MeetingSearchRepositoryCustomImpl implements MeetingSearchRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<MeetingSearchDocument> searchByLocationOrInterestsFuzzy(String query) {
        String elasticsearchQuery = "{\n" +
            "  \"multi_match\": {\n" +
            "    \"query\": \"" + query + "\",\n" +
            "    \"fields\": [\"location^2\", \"interests\"],\n" +
            "    \"fuzziness\": \"AUTO\"\n" +
            "  }\n" +
            "}";

        SearchHits<MeetingSearchDocument> hits = elasticsearchOperations.search(new StringQuery(elasticsearchQuery), MeetingSearchDocument.class);
        return hits.getSearchHits().stream().map(h -> h.getContent()).collect(Collectors.toList());
    }

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

        SearchHits<MeetingSearchDocument> hits = elasticsearchOperations.search(new StringQuery(elasticsearchQuery), MeetingSearchDocument.class);
        return hits.getSearchHits().stream().map(h -> h.getContent()).collect(Collectors.toList());
        }

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

        return hits.getSearchHits().stream().map(h -> h.getContent()).collect(Collectors.toList());
    }
}
