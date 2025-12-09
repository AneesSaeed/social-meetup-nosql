package be.he2b.don5.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.he2b.don5.model.Meeting;
import be.he2b.don5.model.Completion;

public interface MeetingRepository extends MongoRepository<Meeting, String> {
    List<Meeting> findByParticipantsContaining(String userId);
    List<Meeting> findByStatus(Completion status);
}