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
import upc.edu.gessi.repo.service.AnalysisService;
import upc.edu.gessi.repo.service.GraphDBService;
import upc.edu.gessi.repo.service.MobileApplicationService;
import upc.edu.gessi.repo.service.ServiceFactory;


import java.io.File;
import java.util.List;

@RestController
public class MobileApplicationsController implements MobileApplicationsAPI {
    private final Logger logger = LoggerFactory.getLogger(MobileApplicationsController.class);

    private final ExceptionHandlers exceptionHandlers;

    private final ServiceFactory serviceFactory;


    @Autowired
    public MobileApplicationsController(final ExceptionHandlers exceptionHandl,
                                        final ServiceFactory serviceFactory) {
        this.exceptionHandlers = exceptionHandl;
        this.serviceFactory = serviceFactory;
    }

    @Value("${rml.path}")
    private String rmlPath;

    @Override
    public void ping() {
    }

    @Override
    public ResponseEntity<List<MobileApplicationDTO>> create(final List<MobileApplicationDTO> mobileApplications) {
        return new ResponseEntity<>(
                ((MobileApplicationService) useService(MobileApplicationService.class)).create(mobileApplications),
                HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> createViaRMLFormat(final String jsonFolder) {
        try {
            File mappingFile = Utils.getFile(rmlPath);
            ((GraphDBService) useService(GraphDBService.class)).insertRML(jsonFolder, mappingFile);
            return new ResponseEntity<>("RML data inserted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inserting RML data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> createViaRDFFormat(final MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
            }
            ((GraphDBService) useService(GraphDBService.class)).insertRDF(file);
            return new ResponseEntity<>("RDF data inserted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inserting RDF data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<MobileApplicationDTO>> getAllPaginated(final boolean paginated,
                                                                      final Integer page,
                                                                      final Integer size)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return new ResponseEntity<>(((MobileApplicationService) useService(MobileApplicationService.class)).getAllPaginated(paginated, page, size), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<List<MobileApplicationDTO>> getAll() {
        return null;
    }


    @Override
    public ResponseEntity<MobileApplicationDTO> get(final String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<MobileApplicationDTO>> getListed(final List<String> ids) throws ObjectNotFoundException {
        return null;
    }


    @Override
    public ResponseEntity<List<MobileApplicationDTO>> getAllApplicationsNames() throws ApplicationNotFoundException {
        return new ResponseEntity<>(((MobileApplicationService) useService(MobileApplicationService.class)).findAllApplicationNames(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getApplicationFeatures(final String appName) {
        return new ResponseEntity<>(((AnalysisService) useService(AnalysisService.class)).findAppFeatures(appName), HttpStatus.OK);
    }

    @Override
    public void export(final String fileName) throws Exception{
        logger.info("Initializing export...");
        ((GraphDBService) useService(GraphDBService.class)).exportRepository(fileName);
        logger.info("Repository successfully exported at " + fileName);
    }

    @Override
    public ResponseEntity<MobileApplicationDTO> update(final MobileApplicationDTO entity) {
        return null;
    }

    @Override
    public void updateRepository(final String url) {
        logger.info("Updating repo");
        ((GraphDBService) useService(GraphDBService.class)).updateRepository(url);
        logger.info("Repository updated");
    }

    @Override
    public ResponseEntity<Void> delete(String id) {
        return null;
    }

    private Object useService(Class<?> clazz) {
        return serviceFactory.createService(clazz);
    }
}
