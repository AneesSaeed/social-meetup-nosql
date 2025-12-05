package be.he2b.don5.application;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import be.he2b.don5.domain.search.UserSearchDocument;
import be.he2b.don5.domain.search.UserSearchRepository;
import be.he2b.don5.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserSearchRepository searchRepository;
    private final UserRepository userRepository;

    public List<UserSearchDocument> search(String q) {
        return searchRepository.findByNameContainingIgnoreCaseOrBioContainingIgnoreCase(q, q);
    }

    public List<UserSearchDocument> byInterests(List<String> interests) {
        return searchRepository.findByInterestsIn(interests);
    }

    public void syncUser(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            UserSearchDocument doc = new UserSearchDocument();
            doc.setUserId(user.getId());
            doc.setName(user.getName());
            doc.setBio(user.getBio());
            doc.setInterests(user.getInterests());
            doc.setTotalScore(user.getTotalPoints());
            doc.setLastActive(LocalDateTime.now());
            searchRepository.save(doc);
        });
    }

    public void syncAllUsers() {
        List<UserSearchDocument> docs = userRepository.findAll().stream().map(user -> {
            UserSearchDocument doc = new UserSearchDocument();
            doc.setUserId(user.getId());
            doc.setName(user.getName());
            doc.setBio(user.getBio());
            doc.setInterests(user.getInterests());
            doc.setTotalScore(user.getTotalPoints());
            doc.setLastActive(LocalDateTime.now());
            return doc;
        }).toList();
        searchRepository.saveAll(docs);
    }
}

