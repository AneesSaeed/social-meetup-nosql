package be.he2b.don5.search.infrastructure.elasticsearch.document;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import lombok.Data;

/**
 * Elasticsearch document for users.
 *
 * <p>This is the indexed version of a user, stored in the "users" index
 * for fast search.
 */
@Data
@Document(indexName = "users")
public class UserSearchDocument {
    @Id
    private String userId;
    private String name;
    private String email;
    private String bio;
    private List<String> interests;
    private Integer totalScore;
    private LocalDateTime lastActive;
}
