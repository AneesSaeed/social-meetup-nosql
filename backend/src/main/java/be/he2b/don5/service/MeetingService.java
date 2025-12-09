package be.he2b.don5.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.he2b.don5.dto.CreateMeetingRequest;
import be.he2b.don5.model.Meeting;
import be.he2b.don5.model.User;
import be.he2b.don5.repository.MeetingRepository;
import be.he2b.don5.repository.UserRepository;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepo;
    private final UserRepository userRepo;

    public MeetingService(MeetingRepository meetingRepo, UserRepository userRepo) {
        this.meetingRepo = meetingRepo;
        this.userRepo = userRepo;
    }

    @Transactional // pour créer un rollback si jamais une requête échoue
    public Meeting createMeeting(CreateMeetingRequest request) {
        // Vérifier que tous les participants existent
        for (String userId : request.getParticipants()) {
            if (!userRepo.existsById(userId)) {
                throw new RuntimeException("User " + userId + " not found");
            }
        }

        // Créer le meeting
        Meeting meeting = new Meeting(
            request.getParticipants(),
            request.getDate() != null ? request.getDate() : LocalDateTime.now(),
            request.getLocation(),
            request.getInterest(),
            request.getDescription(),
            request.getPoints() > 0 ? request.getPoints() : 10 // Par défaut 10 points
        );

        meeting = meetingRepo.save(meeting);

        // Attribuer les points aux participants et incrémenter totalMeetings
        for (String userId : request.getParticipants()) {
            User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            user.setTotalPoints(user.getTotalPoints() + meeting.getPoints());
            userRepo.save(user);
        }

        return meeting;
    }

    public List<Meeting> getAllMeetings() {
        return meetingRepo.findAll();
    }

    public Meeting getMeetingById(String id) {
        return meetingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Meeting not found"));
    }

    public List<Meeting> getMeetingsByUser(String userId) {
        return meetingRepo.findByParticipantsContaining(userId);
    }

    public List<Meeting> getMeetingsByStatus(String status) {
        return meetingRepo.findByStatus(status);
    }
}