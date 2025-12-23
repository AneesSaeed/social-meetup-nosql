package be.he2b.don5.dto;

import java.util.List;

public record UpdateUserProfileRequest(
        String bio,
        List<String> interests
) {}
