package be.he2b.don5.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Meetings are past event
 */
@Document(collection = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    @Id
    private String id;
    
    private List<String> participants; // Liste d'IDs de users
    private LocalDateTime date;
    private String location;
    private String interest; // Centre d'intérêt commun
    private String description;
    private int points; // Points attribués pour cette rencontre
    private Completion status; // "completed", "cancelled"
    private LocalDateTime createdAt;

    // Constructeur pour création
    public Meeting(List<String> participants, LocalDateTime date, String location, 
                   String interest, String description, int points) {
        this.participants = participants;
        this.date = date;
        this.location = location;
        this.interest = interest;
        this.description = description;
        this.points = points;
        this.status = Completion.COMPLETED;
        this.createdAt = LocalDateTime.now();
    }
}