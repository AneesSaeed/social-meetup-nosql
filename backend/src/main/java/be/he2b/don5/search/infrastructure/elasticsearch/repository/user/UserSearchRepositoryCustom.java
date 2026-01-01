package be.he2b.don5.search.infrastructure.elasticsearch.repository.user;

import java.util.List;

import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;

public interface UserSearchRepositoryCustom {
    List<UserSearchDocument> searchByNameOrBioFuzzy(String query);

    // Fuzzy OR across interests list (at least one interest matches)
    List<UserSearchDocument> searchByInterestsAnyFuzzy(List<String> interests);

    // Fuzzy AND across interests list (all interests must match)
    List<UserSearchDocument> searchByInterestsAllFuzzy(List<String> interests);
}
