package be.he2b.don5.application.dto.user;

import java.util.List;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String bio;
    private List<String> interests;
}

