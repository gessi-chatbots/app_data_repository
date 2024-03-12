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
import upc.edu.gessi.repo.dto.ApplicationDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.service.ApplicationService;
import upc.edu.gessi.repo.service.GraphDBService;

import java.io.File;
import java.util.List;

@RestController
public class GraphDBController <T> implements GraphDBApi<Object> {
    private final Logger logger = LoggerFactory.getLogger(GraphDBController.class);

    private final GraphDBService dbConnection;
    private final ApplicationService applicationService;
    @Autowired
    public GraphDBController(final GraphDBService graphDBService,
                             final ApplicationService applicationService) {
        this.dbConnection = graphDBService;
        this.applicationService = applicationService;
    }

    @Value("${rml.path}")
    private String rmlPath;

    @Override
    public void ping() {
    }

    @Override
    public List<Object> getAllApplications(
            final boolean paginated,
            final Integer page,
            final Integer size,
            final boolean simplified) throws ApplicationNotFoundException {
        return (paginated) ? applicationService.findAllPaginated(page, size, simplified) : applicationService.findAll(simplified);
    }

    @Override
    public List<ApplicationSimplifiedDTO> getAllApplicationsNames() throws ApplicationNotFoundException {
        return applicationService.findAllApplicationNames();
    }

    @Override
    public ApplicationDTO getApplicationData(@PathVariable final String appName) throws ApplicationNotFoundException, ClassNotFoundException, IllegalAccessException {
        return applicationService.findByName(appName.toLowerCase());
    }

    @Override
    public ResponseEntity<String> insertJSONData(@RequestBody List<ApplicationDTO> applicationDTOS) {
        applicationService.insertApps(applicationDTOS);
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

}
