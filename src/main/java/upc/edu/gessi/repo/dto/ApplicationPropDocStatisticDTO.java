package upc.edu.gessi.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPropDocStatisticDTO {
    private String applicationName;
    private Integer reviewFeaturesCount;
    private Integer summaryFeaturesCount;
    private Integer descriptionFeaturesCount;
}
