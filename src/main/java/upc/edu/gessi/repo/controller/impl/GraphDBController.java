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
import upc.edu.gessi.repo.controller.GraphDBApi;
import upc.edu.gessi.repo.dto.*;
import upc.edu.gessi.repo.dto.Analysis.ApplicationDayStatisticsDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopSentimentsDTO;
import upc.edu.gessi.repo.dto.Review.ReviewRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewResponseDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.MissingBodyException;
import upc.edu.gessi.repo.exception.NoReviewsFoundException;
import upc.edu.gessi.repo.exception.ReviewNotFoundException;
import upc.edu.gessi.repo.service.impl.ApplicationServiceImpl;
import upc.edu.gessi.repo.service.impl.GraphDBService;
import upc.edu.gessi.repo.service.impl.ReviewService;
import upc.edu.gessi.repo.service.impl.AnalysisService;


import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class GraphDBController <T> implements GraphDBApi<Object> {
    private final Logger logger = LoggerFactory.getLogger(GraphDBController.class);

    private final GraphDBService dbConnection;
    private final ApplicationServiceImpl applicationServiceImpl;

    private final ReviewService reviewService;

    private final AnalysisService analysisService;
    @Autowired
    public GraphDBController(final GraphDBService graphDBService,
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
    public List<Object> getAllApplications(
            final boolean paginated,
            final Integer page,
            final Integer size,
            final boolean simplified) throws ApplicationNotFoundException {
        return (paginated) ? applicationServiceImpl.findAllPaginated(page, size, simplified) : applicationServiceImpl.findAll(simplified);
    }

    @Override
    public List<ApplicationSimplifiedDTO> getAllApplicationsNames() throws ApplicationNotFoundException {
        return applicationServiceImpl.findAllApplicationNames();
    }

    @Override
    public ApplicationDataDTO getApplicationData(@PathVariable final String appName) throws ApplicationNotFoundException, ClassNotFoundException, IllegalAccessException {
        return applicationServiceImpl.findByName(appName);
    }

    @Override
    public ResponseEntity<String> insertJSONData(@RequestBody List<CompleteApplicationDataDTO> completeApplicationDataDTOS) {
        applicationServiceImpl.insertApps(completeApplicationDataDTOS);
        return new ResponseEntity<>("created", HttpStatus.CREATED);
    }

    @Override
    @PostMapping("/app/rml")
    @ApiOperation(value = "Insert Data (RML format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is mapped from a JSON file located in {jsonFolder} using the RML mapping file specified in {rml.path} property.")
            public ResponseEntity<String> insertRML(@RequestParam("jsonFolder") String jsonFolder) {
        try {

            File mappingFile = Utils.getFile(rmlPath);
            dbConnection.insertRML(jsonFolder, mappingFile);

            return new ResponseEntity<>("RML data inserted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inserting RML data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PostMapping("/app/rdf")
    @ApiOperation(value = "Insert Data (RDF format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent directly in RDF format through a multipart file in Turtle format (.ttl).")
    public ResponseEntity<String> insertRDF(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
            }
            dbConnection.insertRDF(file);
            return new ResponseEntity<>("RDF data inserted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inserting RDF data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*

    @Override
    @PostMapping("/update")
    public int update(@RequestParam(value = "daysFromLastUpdate") int daysFromLastUpdate) {
        dbConnection.updateApps(daysFromLastUpdate);
        return 1;
    }*/

    @Override
    @GetMapping("/export")
    public void export(@RequestParam(value = "fileName") String fileName) throws Exception{
        logger.info("Initializing export...");
        dbConnection.exportRepository(fileName);
        logger.info("Repository successfully exported at " + fileName);
    }

    @Override
    @PostMapping("updateRepository")
    public void updateRepository(@RequestParam(value = "url") String url) {
        logger.info("Updating repo");
        dbConnection.updateRepository(url);
        logger.info("Repository updated");
    }

    @Override
    public List<ReviewResponseDTO> getReviews(List<ReviewRequestDTO> reviews) throws NoReviewsFoundException {
        return reviewService.getAllReviewsData(reviews);
    }

    @Override
    public ReviewResponseDTO getReviewData(String reviewId) throws NoReviewsFoundException {
        return reviewService.getReviewData(reviewId);
    }

    @Override
    public ResponseEntity<String> insertJSONReviewData(List<ReviewResponseDTO> reviewResponseDTOList) {
        reviewService.addReviews(reviewResponseDTOList);
        return new ResponseEntity<>("Reviews inserted successfully", HttpStatus.CREATED);
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
    public ResponseEntity<List<String>> getApplicationFeatures(String appName) {
        return new ResponseEntity<>(analysisService.findAppFeatures(appName), HttpStatus.OK);
    }

    @Override
    public List<ApplicationDayStatisticsDTO> getApplicationStatistics(String appName, Date startDate, Date endDate) {
        if (endDate == null) {
            endDate = Calendar.getInstance().getTime();
        }

        return analysisService.getApplicationStatistics(appName, startDate, endDate);
    }

    @Override
    public ResponseEntity<String> insertSoftwareApplicationJSONData(List<SoftwareApplicationDTO> softwareApplicationDTOS) {
        return null;
    }

    @Override
    public ResponseEntity<String> insertDigitalDocumentsJSONData(List<DigitalDocumentDTO> digitalDocumentDTOS) {
        return null;
    }
}
