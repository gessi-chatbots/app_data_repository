package upc.edu.gessi.repo.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.controller.AnalysisAPI;
import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopDescriptorsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopEmotionsDTO;
import upc.edu.gessi.repo.exception.MissingBodyException;
import upc.edu.gessi.repo.service.AnalysisService;
import upc.edu.gessi.repo.service.InductiveKnowledgeService;
import upc.edu.gessi.repo.service.ServiceFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController implements AnalysisAPI {
    private final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private final ServiceFactory serviceFactory;


    @Autowired
    public AnalysisController(final ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
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
    public TopEmotionsDTO getTopSentimentsByAppNames(final List<String> appNames) throws MissingBodyException {
        validateAppNames(appNames);
        return useAnalysisService().findTopEmotionsByApp(appNames);
    }

    @Override
    public TopDescriptorsDTO getTopDescriptors() {
        return useAnalysisService().findTopDescriptors();
    }

    @Override
    public TopFeaturesDTO getTopFeaturesByAppNames(final List<String> appNames)  throws MissingBodyException {
        validateAppNames(appNames);
        return useAnalysisService().findTopFeaturesByApps(appNames);
    }

    @Override
    public TopFeaturesDTO getTopFeatures(){
        return useAnalysisService().findTopFeatures();
    }

    @Override
    public ResponseEntity<byte[]> generateAnalyticalExcel() throws IOException {
        byte[] spreadsheet = ((InductiveKnowledgeService) useService(InductiveKnowledgeService.class)).generateAnalyticalExcel();
        String spreadsheetFileName = "KG_Feature_Analysis.xlsx";
        String attachmentHeader = "attachment; filename="+spreadsheetFileName;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, attachmentHeader);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().headers(headers).body(spreadsheet);
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

        return useAnalysisService().getApplicationStatistics(appName, startDate, endDateAux);
    }

    private AnalysisService useAnalysisService() {
        return (AnalysisService) serviceFactory.createService(AnalysisService.class);
    }

    private Object useService(Class<?> clazz) {
        return serviceFactory.createService(clazz);
    }



}
