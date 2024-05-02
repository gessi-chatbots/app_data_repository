package upc.edu.gessi.repo.controller.impl;

import be.ugent.rml.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upc.edu.gessi.repo.controller.ExceptionHandlers;
import upc.edu.gessi.repo.controller.MobileApplicationsAPI;
import upc.edu.gessi.repo.dto.*;
import upc.edu.gessi.repo.exception.*;
import upc.edu.gessi.repo.service.impl.AnalysisService;
import upc.edu.gessi.repo.service.impl.ApplicationServiceImpl;
import upc.edu.gessi.repo.service.impl.GraphDBService;

import java.io.File;
import java.util.List;

@RestController
public class MobileApplicationsController implements MobileApplicationsAPI {
    private final Logger logger = LoggerFactory.getLogger(MobileApplicationsController.class);

    private final GraphDBService dbConnection;
    private final ApplicationServiceImpl applicationServiceImpl;

    private final ExceptionHandlers exceptionHandlers;


    private final AnalysisService analysisService;
    @Autowired
    public MobileApplicationsController(final GraphDBService graphDBService,
                                        final ApplicationServiceImpl applicationServiceImpl,
                                        final AnalysisService analysisSv,
                                        final ExceptionHandlers exceptionHandl) {
        this.dbConnection = graphDBService;
        this.applicationServiceImpl = applicationServiceImpl;
        this.analysisService = analysisSv;
        this.exceptionHandlers = exceptionHandl;
    }

    @Value("${rml.path}")
    private String rmlPath;

    @Override
    public void ping() {
    }

    @Override
    public ResponseEntity<MobileApplicationDTO> create(List<MobileApplicationDTO> mobileApplications) {
        // applicationServiceImpl.insertApps(MobileApplicationDTO);
        // return new ResponseEntity<>(applicationServiceImpl.insertApps(MobileApplicationDTO), HttpStatus.CREATED);
        return null;
    }

    @Override
    public ResponseEntity<String> createViaRMLFormat(@RequestParam("jsonFolder") String jsonFolder) {
        try {
            File mappingFile = Utils.getFile(rmlPath);
            dbConnection.insertRML(jsonFolder, mappingFile);
            return new ResponseEntity<>("RML data inserted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inserting RML data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> createViaRDFFormat(MultipartFile file) {
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

    @Override
    public ResponseEntity<List<MobileApplicationDTO>> getAllPaginated(boolean paginated, Integer page, Integer size, boolean simplified) throws ObjectNotFoundException {
        return new ResponseEntity<>((paginated) ? applicationServiceImpl.findAllPaginated(page, size, simplified) : applicationServiceImpl.findAll(simplified), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<List<MobileApplicationDTO>> getAll() {
        return null;
    }


    @Override
    public ResponseEntity<MobileApplicationDTO> get(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<MobileApplicationDTO>> getListed(List<String> id) throws ObjectNotFoundException {
        return null;
    }


    @Override
    public ResponseEntity<List<ApplicationSimplifiedDTO>> getAllApplicationsNames() throws ApplicationNotFoundException {
        return new ResponseEntity<>(applicationServiceImpl.findAllApplicationNames(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getApplicationFeatures(String appName) {
        return new ResponseEntity<>(analysisService.findAppFeatures(appName), HttpStatus.OK);
    }

    @Override
    public void export(@RequestParam(value = "fileName") String fileName) throws Exception{
        logger.info("Initializing export...");
        dbConnection.exportRepository(fileName);
        logger.info("Repository successfully exported at " + fileName);
    }

    @Override
    public ResponseEntity<MobileApplicationDTO> update(MobileApplicationDTO entity) {
        return null;
    }

    @Override
    public void updateRepository(@RequestParam(value = "url") String url) {
        logger.info("Updating repo");
        dbConnection.updateRepository(url);
        logger.info("Repository updated");
    }

    @Override
    public ResponseEntity<Void> delete() {
        return null;
    }
}
