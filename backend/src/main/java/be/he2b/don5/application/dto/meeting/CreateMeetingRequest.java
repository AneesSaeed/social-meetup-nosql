package be.he2b.don5.application.dto.meeting;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMeetingRequest {
    @NotEmpty
    private List<String> participants;
    @NotBlank
    private String interest;
    @NotBlank
    private String location;
    @NotNull
    @Min(1)
    private Integer points;
    private String description;
}

