
package upc.edu.gessi.repo.dto.Analysis;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationDayStatisticsDTO {
    private Date date;
    private List<SentimentOccurrenceDTO> sentimentOccurrences;
    private List<FeatureOccurrenceDTO> featureOccurrences;
}
