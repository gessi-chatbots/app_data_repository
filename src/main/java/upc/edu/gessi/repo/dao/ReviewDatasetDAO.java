package upc.edu.gessi.repo.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewDatasetDAO {
    private String review;
    private String appIdentifier;
    private String feature;
}
