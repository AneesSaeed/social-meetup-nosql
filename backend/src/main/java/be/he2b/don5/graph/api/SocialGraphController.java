package be.he2b.don5.graph.api;

import be.he2b.don5.graph.api.dto.NetworkDto;
import be.he2b.don5.graph.api.dto.RecommendationDto;
import be.he2b.don5.graph.application.SocialGraphService;

import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * REST API for social graph features.
 *
 * Graph writes are handled through Kafka consumers (event-driven).
 * This controller only exposes read endpoints used by the frontend.
 */
@RestController
@RequestMapping("/api/social")
@AllArgsConstructor
public class SocialGraphController {

    private final SocialGraphService socialGraphService;
    
    /**
     * Suggests new users to meet based on friends-of-friends (mutual connections).
     */
    @GetMapping("/recommendations/{userId}")
    public List<RecommendationDto> getRecommendations(@PathVariable String userId) {
        return socialGraphService.getRecommendations(userId);
    }
    
    /**
     * Returns users in the extended network (up to 5 hops) not already met.
     */
    @GetMapping("/network/{userId}")
    public List<NetworkDto> getSocialNetwork(@PathVariable String userId) {
        return socialGraphService.getSocialNetwork(userId);
    }
}