package be.he2b.don5.controller.graph;

import be.he2b.don5.dto.NetworkDto;
import be.he2b.don5.dto.RecommendationDto;
import be.he2b.don5.service.graph.SocialGraphService;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@AllArgsConstructor
public class SocialGraphController {

    private final SocialGraphService socialGraphService;

    @GetMapping("/score/{userId}")
    public Map<String, Object> getUserScore(@PathVariable String userId) {
        Integer score = socialGraphService.getUserTotalScore(userId);
        return Map.of("userId", userId, "totalScore", score);
    }

    @GetMapping("/meetings/{userId}")
    public List<Map<String, Object>> getUserMeetings(@PathVariable String userId) {
        return socialGraphService.getUserMeetings(userId);
    }

    @GetMapping("/recommendations/{userId}")
    public List<RecommendationDto> getRecommendations(@PathVariable String userId) {
        return socialGraphService.getRecommendations(userId);
    }

    @GetMapping("/network/{userId}")
    public List<NetworkDto> getSocialNetwork(@PathVariable String userId) {
        return socialGraphService.getSocialNetwork(userId);
    }
}