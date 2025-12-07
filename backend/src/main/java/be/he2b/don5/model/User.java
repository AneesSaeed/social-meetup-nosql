package be.he2b.don5.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Custom constructor for user creation (register)
    public User(String name, String email, String bio, List<String> interests) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.interests = interests;
        this.totalPoints = 0;
        this.totalMeetings = 0;
    }
}
