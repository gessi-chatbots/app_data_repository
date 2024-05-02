package upc.edu.gessi.repo.controller.impl;

import be.ugent.rml.Utils;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upc.edu.gessi.repo.controller.AnalysisAPI;
import upc.edu.gessi.repo.controller.GraphDBApi;
import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopSentimentsDTO;
import upc.edu.gessi.repo.dto.*;
import upc.edu.gessi.repo.dto.Review.ReviewRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewResponseDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.MissingBodyException;
import upc.edu.gessi.repo.exception.NoReviewsFoundException;
import upc.edu.gessi.repo.exception.ReviewNotFoundException;
import upc.edu.gessi.repo.service.impl.AnalysisService;
import upc.edu.gessi.repo.service.impl.ApplicationServiceImpl;
import upc.edu.gessi.repo.service.impl.GraphDBService;
import upc.edu.gessi.repo.service.impl.ReviewService;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class AnalysisController implements AnalysisAPI {
    private final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    private final GraphDBService dbConnection;
    private final ApplicationServiceImpl applicationServiceImpl;

    private final ReviewService reviewService;

    private final AnalysisService analysisService;
    @Autowired
    public AnalysisController(final GraphDBService graphDBService,
                              final ApplicationServiceImpl applicationServiceImpl,
                              final ReviewService reviewSv,
                              final AnalysisService analysisSv) {
        this.dbConnection = graphDBService;
        this.applicationServiceImpl = applicationServiceImpl;
        reviewService = reviewSv;
        this.analysisService = analysisSv;
    }

    @Value("${rml.path}")
    private String rmlPath;

    @Override
    public void ping() {
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<String> handleApplicationNotFoundException(ApplicationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<String> handleReviewNotFoundException(ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NoReviewsFoundException.class)
    public ResponseEntity<String> handleNoReviewsFoundException(NoReviewsFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MissingBodyException.class)
    public ResponseEntity<String> handleMissingBodyException(MissingBodyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @Override
    public TopSentimentsDTO getTopSentimentsByAppNames(List<String> appNames) throws MissingBodyException {
        if (appNames == null || appNames.isEmpty()) {
            throw new MissingBodyException("missing body");
        }
        return analysisService.findTopSentimentsByApps(appNames);
    }

    @Override
    public TopFeaturesDTO getTopFeaturesByAppNames(List<String> appNames)  throws MissingBodyException {
        if (appNames == null || appNames.isEmpty()) {
            throw new MissingBodyException("missing body");
        }
        return analysisService.findTopFeaturesByApps(appNames);
    }

    @Override
    public List<ApplicationDayStatisticsDTO> getApplicationStatistics(String appName, Date startDate, Date endDate) {
        if (endDate == null) {
            endDate = Calendar.getInstance().getTime();
        }

        return analysisService.getApplicationStatistics(appName, startDate, endDate);
    }

}
