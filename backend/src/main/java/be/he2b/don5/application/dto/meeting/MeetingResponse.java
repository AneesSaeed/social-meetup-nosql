package be.he2b.don5.application.dto.meeting;

import java.time.LocalDateTime;
import java.util.List;
import be.he2b.don5.domain.meeting.Meeting;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MeetingResponse {
    String id;
    List<String> participants;
    LocalDateTime date;
    String location;
    String interest;
    Integer points;
    String status;
    String description;

    public static MeetingResponse from(Meeting meeting) {
        return MeetingResponse.builder()
                .id(meeting.getId())
                .participants(meeting.getParticipants())
                .date(meeting.getDate())
                .location(meeting.getLocation())
                .interest(meeting.getInterest())
                .points(meeting.getPoints())
                .status(meeting.getStatus())
                .description(meeting.getDescription())
                .build();
    }
}

