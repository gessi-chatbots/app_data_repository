package upc.edu.gessi.repo.dto.Review;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewFeatureRequestDTO implements Serializable {

    @JsonProperty("app_id")
    private String appName;

    @JsonProperty("features")
    private List<String> featureList;
}
