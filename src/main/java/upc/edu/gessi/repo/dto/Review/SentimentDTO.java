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
public class SentimentDTO implements Serializable {
    private String sentiment;
}
