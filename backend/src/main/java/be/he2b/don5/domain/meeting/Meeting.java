package be.he2b.don5.domain.meeting;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "meetings")
public class Meeting {
    @Id
    private String id;
    private List<String> participants;
    private LocalDateTime date = LocalDateTime.now();
    private String location;
    private String interest;
    private Integer points;
    private String status = "completed";
    private String description;
}

