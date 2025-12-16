package be.he2b.don5.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    @Id
    private String id;

    // "Event" fields
    private String title;
    private String description;
    private String eventType;        // sport, culture, food, ...
    private LocalDateTime date;
    private String location;
    private String organizer;        // user id
    private List<String> participants;
    private int maxParticipants;
    private List<String> interests; // mulitple interests

    // "Meeting" fields
    private int points;              // points granted when completed (0 if upcoming)
    private Completion status;       // UPCOMING / COMPLETED / CANCELLED
    private LocalDateTime createdAt;

    // Constructor for creation (UPCOMING by default)
    public Meeting(String title, String description, String eventType, LocalDateTime date,
                   String location, String organizer, int maxParticipants, List<String> interests) {
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.date = date;
        this.location = location;
        this.organizer = organizer;
        this.maxParticipants = maxParticipants;
        this.interests = interests;

        this.participants = new ArrayList<>();
        this.participants.add(organizer);

        this.status = Completion.UPCOMING;
        this.points = 0;
        this.createdAt = LocalDateTime.now();
    }
}
