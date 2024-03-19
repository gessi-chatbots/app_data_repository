package upc.edu.gessi.repo.dto.Review;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponseDTO implements Serializable {
    private String reviewId;
    private String review;
    private String applicationId;
    private List<SentenceDTO> sentences;
}
