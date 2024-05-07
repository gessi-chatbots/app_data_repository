package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopSentimentsDTO;

import java.util.Date;
import java.util.List;

public interface AnalysisService {
    List<ApplicationDayStatisticsDTO> getApplicationStatistics(String appName, Date startDate, Date endDate);

    TopSentimentsDTO findTopSentimentsByApps(List<String> appNames);

    TopFeaturesDTO findTopFeaturesByApps(List<String> appNames);

    List<String> findAppFeatures(String appName);
}
