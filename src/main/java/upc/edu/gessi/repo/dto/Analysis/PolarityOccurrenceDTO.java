package upc.edu.gessi.repo.dto.Analysis;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolarityOccurrenceDTO {
    private String polarity;
    private Integer occurrences;
}
