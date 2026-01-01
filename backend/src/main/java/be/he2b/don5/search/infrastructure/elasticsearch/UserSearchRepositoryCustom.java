package be.he2b.don5.search.infrastructure.elasticsearch;

import java.util.List;

public interface UserSearchRepositoryCustom {
    List<UserSearchDocument> searchByNameOrBioFuzzy(String query);

    // Fuzzy OR across interests list (at least one interest matches)
    List<UserSearchDocument> searchByInterestsAnyFuzzy(List<String> interests);

    // Fuzzy AND across interests list (all interests must match)
    List<UserSearchDocument> searchByInterestsAllFuzzy(List<String> interests);
}
