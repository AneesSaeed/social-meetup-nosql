package be.he2b.don5.domain.search;

import java.util.List;

public interface MeetingSearchRepositoryCustom {
    List<MeetingSearchDocument> searchByLocationOrInterestsFuzzy(String query);
    List<MeetingSearchDocument> searchByStatusAndLocationOrInterestsFuzzy(String status, String query);
    List<MeetingSearchDocument> searchByUserIdAndStatusAndLocationOrInterestsFuzzy(
        String userId, String status, String query
    );
}
