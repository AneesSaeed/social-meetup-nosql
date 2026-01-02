package be.he2b.don5.users.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user stored in MongoDB.
 *
 * <p>This entity contains basic profile information and simple counters
 * used by the application (points and number of meetings).
 */
@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String bio;
    private List<String> interests;

    private int totalPoints;
    private int totalMeetings;

    /**
     * Creates a new user during registration.
     *
     * <p>Points and meetings counters start at 0.
     *
     * @param name user display name
     * @param email user email address
     * @param bio short profile description
     * @param interests list of user interests
     */
    public User(String name, String email, String bio, List<String> interests) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.interests = interests;
        this.totalPoints = 0;
        this.totalMeetings = 0;
    }
}
