package be.he2b.don5.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import be.he2b.don5.domain.search.UserSearchDocument;
import be.he2b.don5.domain.search.UserSearchRepository;
import be.he2b.don5.domain.search.MeetingSearchDocument;
import be.he2b.don5.domain.search.MeetingSearchRepository;
import be.he2b.don5.model.User;
import be.he2b.don5.model.Meeting;
import be.he2b.don5.model.Completion;
import be.he2b.don5.repository.UserRepository;
import be.he2b.don5.repository.MeetingRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SearchService {
    
    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingSearchRepository meetingSearchRepository;

    // Synchroniser tous les utilisateurs MongoDB vers Elasticsearch
    @CacheEvict(cacheNames = "search", allEntries = true)
    public void syncAllUsersToElasticsearch() {
        List<User> users = userRepository.findAll();
        List<UserSearchDocument> searchDocs = users.stream()
                .map(this::convertToSearchDocument)
                .collect(Collectors.toList());
        userSearchRepository.saveAll(searchDocs);
    }

    // Synchroniser un utilisateur spécifique
    @CacheEvict(cacheNames = "search", allEntries = true)
    public void syncUserToElasticsearch(User user) {
        UserSearchDocument searchDoc = convertToSearchDocument(user);
        userSearchRepository.save(searchDoc);
    }

    // Recherche par nom ou bio avec fuzzy matching
    @Cacheable(cacheNames = "search", key = "'nameBio:' + #query")
    public List<UserSearchDocument> searchByNameOrBio(String query) {
        return userSearchRepository.searchByNameOrBioFuzzy(query);
    }

    // Recherche par intérêts
    @Cacheable(cacheNames = "search", key = "'interestsAny:' + #interests")
    public List<UserSearchDocument> searchByInterests(List<String> interests) {
        return userSearchRepository.searchByInterestsAnyFuzzy(interests);
    }

    // Recherche par intérêts avec condition AND: l'utilisateur doit contenir TOUS les intérêts
    @Cacheable(cacheNames = "search", key = "'interestsAll:' + #interests")
    public List<UserSearchDocument> searchByInterestsAll(List<String> interests) {
        return userSearchRepository.searchByInterestsAllFuzzy(interests);
    }

    // Conversion User -> UserSearchDocument
    private UserSearchDocument convertToSearchDocument(User user) {
        UserSearchDocument doc = new UserSearchDocument();
        doc.setUserId(user.getId());
        doc.setName(user.getName());
        doc.setEmail(user.getEmail());
        doc.setBio(user.getBio());
        doc.setInterests(user.getInterests());
        doc.setTotalScore(user.getTotalPoints());
        doc.setLastActive(LocalDateTime.now());
        return doc;
    }

    // Meetings indexing and search

    @CacheEvict(cacheNames = "search", allEntries = true)
    public void syncAllMeetingsToElasticsearch() {
        List<Meeting> meetings = meetingRepository.findAll();
        List<MeetingSearchDocument> docs = meetings.stream()
                .map(this::convertToMeetingSearchDocument)
                .collect(Collectors.toList());
        meetingSearchRepository.saveAll(docs);
    }

    @CacheEvict(cacheNames = "search", allEntries = true)
    public void syncMeetingToElasticsearch(Meeting meeting) {
        MeetingSearchDocument doc = convertToMeetingSearchDocument(meeting);
        meetingSearchRepository.save(doc);
    }

    @Cacheable(cacheNames = "search", key = "'meetingsFuzzy:' + #query")
    public List<MeetingSearchDocument> searchMeetingsByLocationOrInterests(String query) {
        return meetingSearchRepository.searchByLocationOrInterestsFuzzy(query);
    }

    @Cacheable(cacheNames = "search", key = "'meetingsByStatusFuzzy:' + #status + ':' + #query")
    public List<MeetingSearchDocument> searchMeetingsByStatusAndLocationOrInterests(Completion status, String query) {
        return meetingSearchRepository.searchByStatusAndLocationOrInterestsFuzzy(status.name(), query);
    }

    private MeetingSearchDocument convertToMeetingSearchDocument(Meeting m) {
        MeetingSearchDocument doc = new MeetingSearchDocument();
        doc.setMeetingId(m.getId());
        doc.setTitle(m.getTitle());
        doc.setEventType(m.getEventType());
        doc.setDate(m.getDate());
        doc.setLocation(m.getLocation());
        doc.setOrganizer(m.getOrganizer());
        doc.setParticipants(m.getParticipants());
        doc.setMaxParticipants(m.getMaxParticipants());
        doc.setInterests(m.getInterests());
        doc.setPoints(m.getPoints());
        doc.setStatus(m.getStatus() != null ? m.getStatus().name().toLowerCase() : null);
        doc.setCreatedAt(m.getCreatedAt());
        return doc;
    }
}
