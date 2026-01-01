package be.he2b.don5.search.infrastructure.elasticsearch;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import lombok.Data;

@Data
@Document(indexName = "meetings")
public class MeetingSearchDocument {
    @Id
    private String meetingId;
    private String title;
    private String eventType;
    private LocalDateTime date;
    private String location;
    private String organizer;
    private List<String> participants;
    private Integer maxParticipants;
    private List<String> interests;
    private Integer points;
    private String status;
    private LocalDateTime createdAt;

    private List<String> userIds;
}

