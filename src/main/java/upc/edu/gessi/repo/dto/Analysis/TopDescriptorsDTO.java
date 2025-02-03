package upc.edu.gessi.repo.dto.Analysis;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopDescriptorsDTO {
    private TopEmotionsDTO topEmotions;
    private TopPolaritiesDTO topPolarities;
    private TopTypesDTO topTypes;
    private TopTopicsDTO topTopics;
}
