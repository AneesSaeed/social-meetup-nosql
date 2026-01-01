package be.he2b.don5.meetings.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.he2b.don5.integration.events.MeetingCancelledEvent;
import be.he2b.don5.integration.events.MeetingCompletedEvent;
import be.he2b.don5.integration.events.MeetingCreatedEvent;
import be.he2b.don5.integration.events.MeetingUpdatedEvent;
import be.he2b.don5.integration.events.UserUpdatedEvent;
import be.he2b.don5.integration.outbox.OutboxWriter;
import be.he2b.don5.meetings.api.dto.CreateMeetingRequest;
import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.meetings.domain.Meeting;
import be.he2b.don5.meetings.infrastructure.mongo.MeetingRepository;
import be.he2b.don5.points.application.PointsCalculationService;
import be.he2b.don5.search.application.SearchService;
import be.he2b.don5.users.domain.User;
import be.he2b.don5.users.infrastructure.mongo.UserRepository;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepo;
    private final UserRepository userRepo;
    private final PointsCalculationService pointsCalculationService;
    private final OutboxWriter outboxService;
    private final SearchService searchService;
    private final CacheManager cacheManager;

    public MeetingService(
            MeetingRepository meetingRepo,
            UserRepository userRepo,
            PointsCalculationService pointsCalculationService,
            OutboxWriter outboxService,
            SearchService searchService,
            CacheManager cacheManager) 
    {
        this.meetingRepo = meetingRepo;
        this.userRepo = userRepo;
        this.pointsCalculationService = pointsCalculationService;
        this.outboxService = outboxService;
        this.searchService = searchService;
        this.cacheManager = cacheManager;
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

        List<String> reqParts = request.getParticipants();
        Set<String> unique = new LinkedHashSet<>();
        unique.add(organizerId);

        if (reqParts != null) {
            for (String id : reqParts) {
                if (id == null) continue;
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) unique.add(trimmed);
            }
        }

        List<String> participants = new ArrayList<>(unique);

        // Validate capacity
        if (participants.size() > max) {
            throw new RuntimeException("Too many participants for maxParticipants=" + max);
        }

        // Validate users exist (skip organizer, already checked)
        for (String pid : participants) {
            if (!pid.equals(organizerId) && !userRepo.existsById(pid)) {
                throw new RuntimeException("User " + pid + " not found");
            }
        }

        // Override participants set by constructor
        meeting.setParticipants(participants);

        Meeting saved = meetingRepo.save(meeting);
        
        // Publier l'événement de création
        MeetingCreatedEvent event = new MeetingCreatedEvent(
            saved.getId(),
            saved.getTitle(),
            saved.getEventType(),
            saved.getDate() != null ? saved.getDate().toString() : null,
            saved.getLocation(),
            saved.getOrganizer(),
            saved.getParticipants(),
            saved.getMaxParticipants(),
            saved.getInterests(),
            saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null
        );
        outboxService.publishEvent(
            saved.getId(),
            "Meeting",
            "MeetingCreatedEvent",
            event
        );
        
        evictStatusCaches(Completion.UPCOMING);
        return saved;
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

    public List<Meeting> getMeetingsByUserAndStatus(String userId, Completion status) {
        return meetingRepo.findByParticipantsContainingAndStatus(userId, status);
    }

    public List<Meeting> getMeetingsByOrganizer(String userId) {
        return meetingRepo.findByOrganizer(userId);
    }

    @Cacheable(cacheNames = "meetingsByStatus", key = "#status.name()")
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
        Meeting saved = meetingRepo.save(meeting);
        
        // Publier l'événement de mise à jour
        MeetingUpdatedEvent event = new MeetingUpdatedEvent(
            saved.getId(),
            saved.getParticipants(),
            "JOIN"
        );
        outboxService.publishEvent(
            saved.getId(),
            "Meeting",
            "MeetingUpdatedEvent",
            event
        );
        
        evictStatusCaches(meeting.getStatus());
        return saved;
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

        Meeting saved = meetingRepo.save(meeting);
        
        // Publier l'événement de mise à jour
        MeetingUpdatedEvent event = new MeetingUpdatedEvent(
            saved.getId(),
            saved.getParticipants(),
            "LEAVE"
        );
        outboxService.publishEvent(
            saved.getId(),
            "Meeting",
            "MeetingUpdatedEvent",
            event
        );
        
        evictStatusCaches(meeting.getStatus());
        return saved;
    }

    /**
     * Marks a meeting as completed and awards points to all participants.
     * Call this from a controller endpoint like:
     * POST /api/meetings/{id}/complete
     */
    @Transactional
    public Meeting completeMeeting(String meetingId) {
        Meeting meeting = getMeetingById(meetingId);

        if (meeting.getStatus() == Completion.CANCELLED) {
            throw new RuntimeException("Cancelled meeting cannot be completed");
        }
        if (meeting.getStatus() == Completion.COMPLETED) {
            return meeting;
        }

        Completion previousStatus = meeting.getStatus();

        meeting.setStatus(Completion.COMPLETED);

        if (meeting.getParticipants() != null && meeting.getParticipants().size() > 0) {

            // Calculer les points avec tous les intÃ©rÃªts du meeting
            Map<String, Integer> pointsPerUser = pointsCalculationService.calculatePointsForMeeting(
                    meeting.getParticipants(),
                    meeting.getInterests());

            int avgPoints = (int) pointsPerUser.values().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(10.0);
            meeting.setPoints(avgPoints);

            // Attribuer points aux participants dans MongoDB + Elasticsearch
            for (String uid : meeting.getParticipants()) {
                User user = userRepo.findById(uid)
                        .orElseThrow(() -> new RuntimeException("User " + uid + " not found"));

                int userPoints = pointsPerUser.getOrDefault(uid, 10);
                user.setTotalPoints(user.getTotalPoints() + userPoints);
                user.setTotalMeetings(user.getTotalMeetings() + 1);
                userRepo.save(user);

                UserUpdatedEvent userEvent = new UserUpdatedEvent(
                    user.getId(),
                    user.getBio(),
                    user.getInterests(),
                    user.getTotalPoints(),
                    user.getTotalMeetings()
                );
                
                outboxService.publishEvent(
                    user.getId(),
                    "User",
                    "UserUpdatedEvent",
                    userEvent
                );
                evictUserAndSearchCaches(user.getId());
            }

            String interestsStr = meeting.getInterests() != null && !meeting.getInterests().isEmpty()
                    ? String.join(", ", meeting.getInterests())
                    : meeting.getEventType();

            MeetingCompletedEvent meetingEvent = new MeetingCompletedEvent(
                meeting.getId(),
                meeting.getParticipants(),
                pointsPerUser,
                meeting.getDate().toString(),
                meeting.getLocation(),
                interestsStr
            );
            
            outboxService.publishEvent(
                meeting.getId(),
                "Meeting",
                "MeetingCompletedEvent",
                meetingEvent
            );
        }

        Meeting saved = meetingRepo.save(meeting);
        evictStatusCaches(previousStatus, meeting.getStatus());
        return saved;
    }

    @Transactional
    public Meeting cancelMeeting(String meetingId) {
        Meeting meeting = getMeetingById(meetingId);

        if (meeting.getStatus() == Completion.COMPLETED) {
            throw new RuntimeException("Completed meeting cannot be cancelled");
        }

        Completion previousStatus = meeting.getStatus();

        meeting.setStatus(Completion.CANCELLED);
        Meeting saved = meetingRepo.save(meeting);
        
        // Publier l'événement d'annulation
        MeetingCancelledEvent event = new MeetingCancelledEvent(saved.getId());
        outboxService.publishEvent(
            saved.getId(),
            "Meeting",
            "MeetingCancelledEvent",
            event
        );
        
        evictStatusCaches(previousStatus, meeting.getStatus());
        return saved;
    }

    private void evictUserAndSearchCaches(String userId) {
        if (cacheManager == null) return;

        Cache userCache = cacheManager.getCache("user");
        if (userCache != null) {
            userCache.evict(userId);
        }

        Cache searchCache = cacheManager.getCache("search");
        if (searchCache != null) {
            searchCache.clear();
        }
    }

    private void evictStatusCaches(Completion... statuses) {
        if (cacheManager == null) return;

        Cache cache = cacheManager.getCache("meetingsByStatus");
        if (cache == null) return;

        if (statuses == null || statuses.length == 0) {
            cache.clear();
            return;
        }

        for (Completion status : statuses) {
            if (status != null) {
                cache.evict(status.name());
            }
        }
    }
}