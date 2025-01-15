package upc.edu.gessi.repo.dto.Review;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentenceDTO implements Serializable {
    private String id;
    private SentimentDTO sentimentData;
    private FeatureDTO featureData;
    private PolarityDTO polarityData;   
    private TopicDTO topicData;
    private TypeDTO typeData;
    private String text;

}
