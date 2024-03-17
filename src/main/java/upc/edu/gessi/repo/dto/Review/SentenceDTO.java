package upc.edu.gessi.repo.dto.Review;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentenceDTO implements Serializable {
    private String id;
    private SentimentDTO sentimentData;
    private FeatureDTO featureData;
}
