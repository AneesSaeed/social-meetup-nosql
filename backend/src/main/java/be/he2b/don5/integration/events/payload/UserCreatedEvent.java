package be.he2b.don5.integration.events.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent when a user is created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    /**
     * Created user id.
     */
    private String userId;

    /**
     * User display name.
     */
    private String name;

    /**
     * User email address.
     */
    private String email;

    /**
     * User bio.
     */
    private String bio;

    /**
     * User interests.
     */
    private List<String> interests;

    /**
     * Initial total points.
     */
    private int totalPoints;
}
