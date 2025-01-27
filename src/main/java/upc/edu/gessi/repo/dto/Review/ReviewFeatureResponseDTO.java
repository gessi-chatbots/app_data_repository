package upc.edu.gessi.repo.dto.Review;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewFeatureResponseDTO implements Serializable {

    @JsonProperty("reviewId")
    private String id;

    @JsonProperty("review")
    private String reviewText;

    private List<FeatureDTO> featureDTOs;

    private List<PolarityDTO> polarityDTOs;

    private List<TypeDTO> typeDTOs;

    private List<TopicDTO> topicDTOs;
}
