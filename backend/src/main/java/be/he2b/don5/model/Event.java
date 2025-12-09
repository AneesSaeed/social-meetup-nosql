package be.he2b.don5.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    private String id;
    
    private String title;
    private String description;
    private String eventType; // "sport", "culture", "food", etc.
    private LocalDateTime date;
    private String location;
    private String organizer; // User ID de l'organisateur
    private List<String> participants; // Liste d'IDs de users
    private int maxParticipants;
    private String interest; // Tags/centres d'intérêt
    private Completion status; // "upcoming", "cancelled", "completed"
    private LocalDateTime createdAt;

    // Constructeur pour création
    public Event(String title, String description, String eventType, LocalDateTime date,
                 String location, String organizer, int maxParticipants, String interest) {
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.date = date;
        this.location = location;
        this.organizer = organizer;
        this.maxParticipants = maxParticipants;
        this.interest = interest;
        this.participants = List.of(organizer); // L'organisateur participe automatiquement
        this.status = Completion.UPCOMING;
        this.createdAt = LocalDateTime.now();
    }
}