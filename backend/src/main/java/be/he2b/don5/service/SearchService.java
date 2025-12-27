package be.he2b.don5.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import be.he2b.don5.domain.search.UserSearchDocument;
import be.he2b.don5.domain.search.UserSearchRepository;
import be.he2b.don5.model.User;
import be.he2b.don5.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SearchService {
    
    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;

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
}
