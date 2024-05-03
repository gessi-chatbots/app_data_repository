package upc.edu.gessi.repo.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.controller.AnalysisAPI;
import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopSentimentsDTO;
import upc.edu.gessi.repo.exception.MissingBodyException;
import upc.edu.gessi.repo.service.impl.AnalysisServiceImpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class AnalysisController implements AnalysisAPI {
    private final Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    private final AnalysisServiceImpl analysisServiceImpl;

    @Autowired
    public AnalysisController(final AnalysisServiceImpl analysisSv) {
        this.analysisServiceImpl = analysisSv;
    }

    @Override
    public void ping() {
    }

    private void validateAppNames(final List<String> appNames) throws MissingBodyException {
        if (appNames == null || appNames.isEmpty()) {
            throw new MissingBodyException("missing body");
        }
    }
    @Override
    public TopSentimentsDTO getTopSentimentsByAppNames(final List<String> appNames) throws MissingBodyException {
        validateAppNames(appNames);
        return analysisServiceImpl.findTopSentimentsByApps(appNames);
    }

    @Override
    public TopFeaturesDTO getTopFeaturesByAppNames(final List<String> appNames)  throws MissingBodyException {
        validateAppNames(appNames);
        return analysisServiceImpl.findTopFeaturesByApps(appNames);
    }

    @Override
    public List<ApplicationDayStatisticsDTO> getApplicationStatistics(final String appName,
                                                                      final Date startDate,
                                                                      final Date endDate) {
        Date endDateAux = endDate;
        if (endDateAux == null) {
            logger.warn("No end date given, using today as end ate");
            endDateAux = Calendar.getInstance().getTime();
        }

        return analysisServiceImpl.getApplicationStatistics(appName, startDate, endDateAux);
    }

}
