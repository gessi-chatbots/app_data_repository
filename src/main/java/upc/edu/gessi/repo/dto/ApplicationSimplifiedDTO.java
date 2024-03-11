package upc.edu.gessi.repo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationSimplifiedDTO implements Serializable {
    private String name;
    private String author;
    private Integer reviewCount;
}