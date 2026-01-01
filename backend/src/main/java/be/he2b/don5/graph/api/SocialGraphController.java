package be.he2b.don5.graph.api;

import be.he2b.don5.graph.api.dto.NetworkDto;
import be.he2b.don5.graph.api.dto.RecommendationDto;
import be.he2b.don5.graph.application.SocialGraphService;

import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/social")
@AllArgsConstructor
public class SocialGraphController {

    private final SocialGraphService socialGraphService;

    @GetMapping("/network/{userId}")
    public List<NetworkDto> getSocialNetwork(@PathVariable String userId) {
        return socialGraphService.getSocialNetwork(userId);
    }
}