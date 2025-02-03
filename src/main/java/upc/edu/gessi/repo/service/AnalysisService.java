package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopDescriptorsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopEmotionsDTO;

import java.util.Date;
import java.util.List;

public interface AnalysisService {
    List<ApplicationDayStatisticsDTO> getApplicationStatistics(String appName, String descriptor, Date startDate, Date endDate);

    TopEmotionsDTO findTopEmotionsByApp(List<String> appNames);

    TopFeaturesDTO findTopFeaturesByApps(List<String> appNames);
    TopFeaturesDTO findTopFeatures();

    List<String> findAppFeatures(String appName);

    TopDescriptorsDTO findTopDescriptors();
}
