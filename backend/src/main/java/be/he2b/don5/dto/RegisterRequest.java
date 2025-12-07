package be.he2b.don5.dto;

import java.util.List;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String bio;
    private List<String> interests;
}
