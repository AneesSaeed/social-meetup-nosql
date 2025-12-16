package be.he2b.don5.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.he2b.don5.dto.CreateMeetingRequest;
import be.he2b.don5.model.Completion;
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

    @Transactional
    public Meeting createMeeting(CreateMeetingRequest request) {
        // organizer must exist
        String organizerId = request.getOrganizer();
        if (organizerId == null || organizerId.isBlank()) {
            throw new RuntimeException("Organizer is required");
        }
        if (!userRepo.existsById(organizerId)) {
            throw new RuntimeException("User " + organizerId + " not found");
        }

        // sensible defaults
        LocalDateTime date = request.getDate() != null ? request.getDate() : LocalDateTime.now();
        int max = request.getMaxParticipants() > 0 ? request.getMaxParticipants() : 10;

        Meeting meeting = new Meeting(
            request.getTitle(),
            request.getDescription(),
            request.getEventType(),
            date,
            request.getLocation(),
            organizerId,
            max,
            request.getInterests()
        );

        return meetingRepo.save(meeting);
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

    public List<Meeting> getMeetingsByOrganizer(String userId) {
        return meetingRepo.findByOrganizer(userId);
    }

    public List<Meeting> getMeetingsByStatus(Completion status) {
        return meetingRepo.findByStatus(status);
    }

    public List<Meeting> searchMeetingsByInterest(String interest) {
        return meetingRepo.findByInterestsRegexIgnoreCase(interest);
    }

    @Transactional
    public Meeting joinMeeting(String meetingId, String userId) {
        if (!userRepo.existsById(userId)) {
            throw new RuntimeException("User " + userId + " not found");
        }

        Meeting meeting = getMeetingById(meetingId);

        if (meeting.getStatus() != Completion.UPCOMING) {
            throw new RuntimeException("Only UPCOMING meetings can be joined");
        }

        List<String> participants = meeting.getParticipants();
        if (participants == null) {
            participants = new ArrayList<>();
            meeting.setParticipants(participants);
        }

        if (participants.contains(userId)) {
            return meeting; // already joined
        }

        if (meeting.getMaxParticipants() > 0 && participants.size() >= meeting.getMaxParticipants()) {
            throw new RuntimeException("Meeting is full");
        }

        participants.add(userId);
        return meetingRepo.save(meeting);
    }

    @Transactional
    public Meeting leaveMeeting(String meetingId, String userId) {
        Meeting meeting = getMeetingById(meetingId);

        if (meeting.getStatus() != Completion.UPCOMING) {
            throw new RuntimeException("Only UPCOMING meetings can be left");
        }

        if (meeting.getOrganizer() != null && meeting.getOrganizer().equals(userId)) {
            throw new RuntimeException("Organizer cannot leave their own meeting");
        }

        List<String> participants = meeting.getParticipants();
        if (participants != null) {
            participants.remove(userId);
        }

        return meetingRepo.save(meeting);
    }

    /**
     * Marks a meeting as completed and awards points to all participants.
     * Call this from a controller endpoint like:
     * POST /api/meetings/{id}/complete?points=10
     */
    @Transactional
    public Meeting completeMeeting(String meetingId, int points) {
        Meeting meeting = getMeetingById(meetingId);

        if (meeting.getStatus() == Completion.CANCELLED) {
            throw new RuntimeException("Cancelled meeting cannot be completed");
        }
        if (meeting.getStatus() == Completion.COMPLETED) {
            return meeting; // idempotent
        }

        int awardedPoints = points > 0 ? points : 10;

        meeting.setStatus(Completion.COMPLETED);
        meeting.setPoints(awardedPoints);

        Meeting saved = meetingRepo.save(meeting);

        // award points + meeting count to all participants
        if (saved.getParticipants() != null) {
            for (String uid : saved.getParticipants()) {
                User user = userRepo.findById(uid)
                    .orElseThrow(() -> new RuntimeException("User " + uid + " not found"));

                user.setTotalPoints(user.getTotalPoints() + awardedPoints);
                user.setTotalMeetings(user.getTotalMeetings() + 1);
                userRepo.save(user);
            }
        }

        return saved;
    }

    @Transactional
    public Meeting cancelMeeting(String meetingId) {
        Meeting meeting = getMeetingById(meetingId);

        if (meeting.getStatus() == Completion.COMPLETED) {
            throw new RuntimeException("Completed meeting cannot be cancelled");
        }

        meeting.setStatus(Completion.CANCELLED);
        return meetingRepo.save(meeting);
    }
}
