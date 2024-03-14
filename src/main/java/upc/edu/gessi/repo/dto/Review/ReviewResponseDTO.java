package upc.edu.gessi.repo.dto.Review;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO implements Serializable {
    @JsonProperty("reviewId")
    private String id;
    @JsonProperty("review")
    private String body;
    private List<SentenceDTO> sentences;
}
