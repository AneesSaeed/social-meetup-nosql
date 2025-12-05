package be.he2b.don5.domain.meeting;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MeetingRepository extends MongoRepository<Meeting, String> {
    List<Meeting> findByParticipantsContaining(String userId);
}

