package be.he2b.don5.search.application;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import be.he2b.don5.meetings.domain.Completion;
import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;
import be.he2b.don5.search.infrastructure.elasticsearch.repository.meeting.MeetingSearchRepository;
import be.he2b.don5.search.infrastructure.elasticsearch.repository.user.UserSearchRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SearchService {
    
    private final UserSearchRepository userSearchRepository;
    private final MeetingSearchRepository meetingSearchRepository;

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

    @Cacheable(cacheNames = "search", key = "'meetingsFuzzy:' + #query")
    public List<MeetingSearchDocument> searchMeetingsByLocationOrInterests(String query) {
        return meetingSearchRepository.searchByLocationOrInterestsFuzzy(query);
    }

    @Cacheable(cacheNames = "search", key = "'meetingsByStatusFuzzy:' + #status + ':' + #query")
    public List<MeetingSearchDocument> searchMeetingsByStatusAndLocationOrInterests(Completion status, String query) {
        return meetingSearchRepository.searchByStatusAndLocationOrInterestsFuzzy(status.name(), query);
    }

    public List<MeetingSearchDocument> searchMeetingsByUserStatusAndQuery(String userId, Completion status, String query) {
        return meetingSearchRepository.searchByUserIdAndStatusAndLocationOrInterestsFuzzy(
            userId, 
            status.name(), 
            query
        );
    }
}
