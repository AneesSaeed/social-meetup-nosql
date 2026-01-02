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

import be.he2b.don5.integration.events.AggregateType;
import be.he2b.don5.integration.events.EventType;
import be.he2b.don5.integration.events.payload.MeetingCancelledEvent;
import be.he2b.don5.integration.events.payload.MeetingCompletedEvent;
import be.he2b.don5.integration.events.payload.MeetingCreatedEvent;
import be.he2b.don5.integration.events.payload.MeetingUpdatedEvent;
import be.he2b.don5.integration.events.payload.UserUpdatedEvent;
import be.he2b.don5.integration.outbox.OutboxWriter;
import be.he2b.don5.meetings.api.dto.CreateMeetingRequest;
import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.meetings.domain.Meeting;
import be.he2b.don5.meetings.infrastructure.mongo.MeetingRepository;
import be.he2b.don5.points.PointsCalculationService;
import be.he2b.don5.search.application.SearchService;
import be.he2b.don5.users.domain.User;
import be.he2b.don5.users.infrastructure.mongo.UserRepository;

/**
 * Application service for meeting operations.
 *
 * <p>This service:
 * <ul>
 *   <li>creates meetings</li>
 *   <li>lets users join and leave meetings</li>
 *   <li>marks meetings as completed or cancelled</li>
 *   <li>awards points to users when a meeting is completed</li>
 *   <li>publishes events using the outbox mechanism</li>
 *   <li>clears caches when needed</li>
 * </ul>
 * 
 */
@Service
public class MeetingService {

    /**
     * MongoDB repository for meetings.
     */
    private final MeetingRepository meetingRepo;
    /**
     * MongoDB repository for users (validation and points update).
     */
    private final UserRepository userRepo;
    /**
     * Service used to compute points for participants when a meeting completes.
     */
    private final PointsCalculationService pointsCalculationService;
    /**
     * Outbox writer used to publish meeting/user events (Kafka).
     */    
    private final OutboxWriter outboxService;
    /**
     * Cache manager used to clear user/search/meeting caches.
     */
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
        this.cacheManager = cacheManager;
    }
    
    /**
     * Creates a new meeting (status is UPCOMING).
     *
     * <p>Validates that the organizer exists, applies defaults, ensures participants
     * are unique and within the capacity, saves the meeting, and publishes a
     * {@link MeetingCreatedEvent}.
     *
     * @param request meeting creation request
     * @return created meeting
     */
    @Transactional
    public Meeting createMeeting(CreateMeetingRequest request) {
        String organizerId = request.getOrganizer();
        if (organizerId == null || organizerId.isBlank()) {
            throw new RuntimeException("Organizer is required");
        }
        if (!userRepo.existsById(organizerId)) {
            throw new RuntimeException("User " + organizerId + " not found");
        }

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

        if (participants.size() > max) {
            throw new RuntimeException("Too many participants for maxParticipants=" + max);
        }

        for (String pid : participants) {
            if (!pid.equals(organizerId) && !userRepo.existsById(pid)) {
                throw new RuntimeException("User " + pid + " not found");
            }
        }

        meeting.setParticipants(participants);

        Meeting saved = meetingRepo.save(meeting);
        
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
        outboxService.addEvent(
            saved.getId(),
            AggregateType.MEETING,
            EventType.MEETING_CREATED,
            event
        );
        
        evictStatusCaches(Completion.UPCOMING);
        return saved;
    }
    
    /**
     * Returns all meetings.
     *
     * @return list of meetings
     */
    public List<Meeting> getAllMeetings() {
        return meetingRepo.findAll();
    }
    
    /**
     * Returns one meeting by id.
     *
     * @param id meeting id
     * @return meeting
     * @throws RuntimeException if not found
     */
    public Meeting getMeetingById(String id) {
        return meetingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
    }
    
    /**
     * Returns meetings where the given user is in the participants list.
     *
     * @param userId user id
     * @return list of meetings
     */
    public List<Meeting> getMeetingsByUser(String userId) {
        return meetingRepo.findByParticipantsContaining(userId);
    }
    
    /**
     * Returns meetings for a user filtered by status.
     *
     * @param userId user id
     * @param status meeting status
     * @return list of meetings
     */
    public List<Meeting> getMeetingsByUserAndStatus(String userId, Completion status) {
        return meetingRepo.findByParticipantsContainingAndStatus(userId, status);
    }

    /**
     * Returns meetings filtered by status (cached).
     *
     * @param status meeting status
     * @return list of meetings
     */
    @Cacheable(cacheNames = "meetingsByStatus", key = "#status.name()")
    public List<Meeting> getMeetingsByStatus(Completion status) {
        return meetingRepo.findByStatus(status);
    }

    /**
     * Adds a user to a meeting.
     *
     * <p>Rules:
     * <ul>
     *   <li>user must exist</li>
     *   <li>meeting must be UPCOMING</li>
     *   <li>meeting must not be full</li>
     * </ul>
     * Publishes a {@link MeetingUpdatedEvent} with action "JOIN".
     *
     * @param meetingId meeting id
     * @param userId user id to join
     * @return updated meeting
     */
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
        
        MeetingUpdatedEvent event = new MeetingUpdatedEvent(
            saved.getId(),
            saved.getParticipants(),
            "JOIN"
        );
        outboxService.addEvent(
            saved.getId(),
            AggregateType.MEETING,
            EventType.MEETING_UPDATED,
            event
        );
        
        evictStatusCaches(meeting.getStatus());
        return saved;
    }

    /**
     * Removes a user from a meeting.
     *
     * <p>Rules:
     * <ul>
     *   <li>meeting must be UPCOMING</li>
     *   <li>organizer cannot leave their own meeting</li>
     * </ul>
     * Publishes a {@link MeetingUpdatedEvent} with action "LEAVE".
     *
     * @param meetingId meeting id
     * @param userId user id to leave
     * @return updated meeting
     */
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
        
        MeetingUpdatedEvent event = new MeetingUpdatedEvent(
            saved.getId(),
            saved.getParticipants(),
            "LEAVE"
        );
        outboxService.addEvent(
            saved.getId(),
            AggregateType.MEETING,
            EventType.MEETING_UPDATED,
            event
        );
        
        evictStatusCaches(meeting.getStatus());
        return saved;
    }

    /**
     * Marks a meeting as completed and awards points to participants.
     *
     * <p>Rules:
     * <ul>
     *   <li>cancelled meetings cannot be completed</li>
     *   <li>if already completed, it returns the meeting</li>
     * </ul>
     * When completed:
     * <ul>
     *   <li>calculates points for each participant</li>
     *   <li>updates users totalPoints and totalMeetings</li>
     *   <li>publishes {@link UserUpdatedEvent} and {@link MeetingCompletedEvent}</li>
     * </ul>
     * 
     *
     * @param meetingId meeting id
     * @return updated meeting
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

            Map<String, Integer> pointsPerUser = pointsCalculationService.calculatePointsForMeeting(
                    meeting.getParticipants(),
                    meeting.getInterests());

            int avgPoints = (int) pointsPerUser.values().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(10.0);
            meeting.setPoints(avgPoints);

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
                
                outboxService.addEvent(
                    user.getId(),
                    AggregateType.USER,
                    EventType.USER_UPDATED,
                    userEvent
                );
                evictUserAndSearchCaches(user.getId());
            }

            MeetingCompletedEvent meetingEvent = new MeetingCompletedEvent(
                meeting.getId(),
                meeting.getParticipants(),
                pointsPerUser,
                meeting.getDate().toString(),
                meeting.getLocation(),
                meeting.getInterests()
            );
            
            outboxService.addEvent(
                meeting.getId(),
                AggregateType.MEETING,
                EventType.MEETING_COMPLETED,
                meetingEvent
            );
        }

        Meeting saved = meetingRepo.save(meeting);
        evictStatusCaches(previousStatus, meeting.getStatus());
        return saved;
    }

    /**
     * Cancels a meeting.
     *
     * <p>Rules:
     * <ul>
     *   <li>completed meetings cannot be cancelled</li>
     * </ul>
     * Publishes a {@link MeetingCancelledEvent}.
     *
     * @param meetingId meeting id
     * @return updated meeting
     */
    @Transactional
    public Meeting cancelMeeting(String meetingId) {
        Meeting meeting = getMeetingById(meetingId);

        if (meeting.getStatus() == Completion.COMPLETED) {
            throw new RuntimeException("Completed meeting cannot be cancelled");
        }

        Completion previousStatus = meeting.getStatus();

        meeting.setStatus(Completion.CANCELLED);
        Meeting saved = meetingRepo.save(meeting);
        
        MeetingCancelledEvent event = new MeetingCancelledEvent(saved.getId());
        outboxService.addEvent(
            saved.getId(),
            AggregateType.MEETING,
            EventType.MEETING_CANCELLED,
            event
        );
        
        evictStatusCaches(previousStatus, meeting.getStatus());
        return saved;
    }

    // ------------------------
    // ------- HELPERS --------
    // ------------------------

    /**
     * Clears caches related to a single user and the global search cache.
     *
     * @param userId user id
     */
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

    /**
     * Clears the "meetingsByStatus" cache for one or more statuses.
     *
     * @param statuses statuses to evict (if empty, clears the whole cache)
     */
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