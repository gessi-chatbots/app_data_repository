package upc.edu.gessi.repo.dto.Review;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentenceDTO implements Serializable {
    private String id;
    private String sentiment;
    private String feature;
}
