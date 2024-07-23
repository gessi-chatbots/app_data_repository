package upc.edu.gessi.repo.dao;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSentenceAndFeatureDAO {
    @JsonProperty("sentenceId")
    private String sentenceId;

    @JsonProperty("sentence")
    private String sentence;

    @JsonProperty("feature")
    private String feature;
}
