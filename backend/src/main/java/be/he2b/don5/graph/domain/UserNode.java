package be.he2b.don5.graph.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Neo4j node representing a user in the social graph.
 *
 * It only stores the minimum data needed for graph queries (id + name).
 *
 * Label: (User)
 * Key: id
 */
@Node("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNode {
    /** Same id as the MongoDB user id. */
    @Id
    private String id;
    private String name;
}
