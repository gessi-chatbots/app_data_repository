package upc.edu.gessi.repo.dao;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSentenceAndFeatureDAO {
    @JsonProperty("sentence_id")
    private String sentenceId;

    @JsonProperty("sentence_text")
    private String sentence;

    @JsonProperty("feature_type")
    private String feature;
}
