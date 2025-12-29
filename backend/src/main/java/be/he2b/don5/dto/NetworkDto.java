package be.he2b.don5.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDto {
    private String userId;
    private String userName;
    private Integer distance;
}
