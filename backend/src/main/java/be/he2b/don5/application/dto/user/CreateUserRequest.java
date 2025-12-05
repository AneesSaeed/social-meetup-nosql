package be.he2b.don5.application.dto.user;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    private String name;
    @Email
    private String email;
    private String bio;
    @NotEmpty
    private List<String> interests;
}

