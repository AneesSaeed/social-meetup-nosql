package be.he2b.don5.meetings.infrastructure.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.meetings.domain.Meeting;

public interface MeetingRepository extends MongoRepository<Meeting, String> {
    List<Meeting> findByStatus(Completion status);
    List<Meeting> findByOrganizer(String organizer);
    List<Meeting> findByParticipantsContaining(String userId);
    List<Meeting> findByParticipantsContainingAndStatus(String userId, Completion status);
    @Query("{ 'interests': { $elemMatch: { $regex: ?0, $options: 'i' } } }")
    List<Meeting> findByInterestsRegexIgnoreCase(String interest);
}
