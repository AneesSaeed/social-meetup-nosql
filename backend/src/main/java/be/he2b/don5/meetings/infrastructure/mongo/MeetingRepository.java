package be.he2b.don5.meetings.infrastructure.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.meetings.domain.Meeting;

/**
 * MongoDB repository for {@link Meeting}.
 *
 * <p>Provides basic CRUD operations and a few queries used by the meeting module.
 */
public interface MeetingRepository extends MongoRepository<Meeting, String> {
    /**
     * Returns meetings with the given status.
     *
     * @param status meeting status
     * @return matching meetings
     */
    List<Meeting> findByStatus(Completion status);
    /**
     * Returns meetings organized by the given user.
     *
     * @param organizer organizer user id
     * @return matching meetings
     */
    List<Meeting> findByOrganizer(String organizer);
    /**
     * Returns meetings where the given user is in the participants list.
     *
     * @param userId user id
     * @return matching meetings
     */
    List<Meeting> findByParticipantsContaining(String userId);
    /**
     * Returns meetings where the given user is a participant and the meeting has a given status.
     *
     * @param userId user id
     * @param status meeting status
     * @return matching meetings
     */
    List<Meeting> findByParticipantsContainingAndStatus(String userId, Completion status);
    /**
     * Searches meetings by one interest value using a case-insensitive regex.
     *
     * @param interest interest text to match
     * @return meetings where interests contains a matching value
     */
    @Query("{ 'interests': { $elemMatch: { $regex: ?0, $options: 'i' } } }")
    List<Meeting> findByInterestsRegexIgnoreCase(String interest);
}
