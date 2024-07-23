package upc.edu.gessi.repo.dao;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SentenceAndFeatureDAO {
    @JsonProperty("sentence")
    private String sentence;
    @JsonProperty("feature")
    private String feature;
}
