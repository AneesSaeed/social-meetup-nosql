package be.he2b.don5.search.infrastructure.elasticsearch.repository.meeting;

import java.util.List;

import be.he2b.don5.search.infrastructure.elasticsearch.document.MeetingSearchDocument;

public interface MeetingSearchRepositoryCustom {
    List<MeetingSearchDocument> searchByLocationOrInterestsFuzzy(String query);
    List<MeetingSearchDocument> searchByStatusAndLocationOrInterestsFuzzy(String status, String query);
    List<MeetingSearchDocument> searchByUserIdAndStatusAndLocationOrInterestsFuzzy(
        String userId, String status, String query
    );
}
