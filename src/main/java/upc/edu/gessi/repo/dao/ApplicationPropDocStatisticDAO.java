package upc.edu.gessi.repo.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPropDocStatisticDAO {
    private String applicationName;
    private Integer reviewFeaturesCount;
    private Integer summaryFeaturesCount;
    private Integer descriptionFeaturesCount;
    private Date firstDate;
    private Date endDate;
}
