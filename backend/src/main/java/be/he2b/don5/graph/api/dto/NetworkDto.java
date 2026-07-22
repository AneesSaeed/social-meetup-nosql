package be.he2b.don5.graph.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API DTO representing a user found in the extended network.
 *
 * distance = number of hops in the shortest path in Neo4j.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDto {
    private String userId;
    private String userName;
    private Integer distance;
}
