package be.he2b.don5.integration.events.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String userId;
    private String name;
    private String email;
    private String bio;
    private List<String> interests;
    private int totalPoints;
}