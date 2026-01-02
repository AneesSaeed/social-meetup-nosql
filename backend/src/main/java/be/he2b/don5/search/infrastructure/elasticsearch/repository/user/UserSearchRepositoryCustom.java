package be.he2b.don5.search.infrastructure.elasticsearch.repository.user;

import java.util.List;

import be.he2b.don5.search.infrastructure.elasticsearch.document.UserSearchDocument;

/**
 * Custom Elasticsearch queries for users.
 *
 * <p>These methods build fuzzy queries to search users by text fields.</p>
 */
public interface UserSearchRepositoryCustom {
    /**
     * Searches users by name or bio (fuzzy).
     *
     * @param query search text
     * @return matching users
     */    
    List<UserSearchDocument> searchByNameOrBioFuzzy(String query);


    /**
     * Searches users by interests (OR condition, fuzzy).
     *
     * <p>At least one interest must match.</p>
     *
     * @param interests list of interests
     * @return matching users (empty list if input is empty)
     */
    List<UserSearchDocument> searchByInterestsAnyFuzzy(List<String> interests);

    /**
     * Searches users by interests (AND condition, fuzzy).
     *
     * <p>All interests must match.</p>
     *
     * @param interests list of interests
     * @return matching users (empty list if input is empty)
     */
    List<UserSearchDocument> searchByInterestsAllFuzzy(List<String> interests);
}
