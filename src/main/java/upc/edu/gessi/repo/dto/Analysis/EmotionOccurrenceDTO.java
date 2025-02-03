package upc.edu.gessi.repo.dto.Analysis;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmotionOccurrenceDTO {
    private String emotion;
    private Integer occurrences;
}
