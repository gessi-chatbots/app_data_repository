package upc.edu.gessi.repo.controller;

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
import springfox.documentation.annotations.ApiIgnore;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.service.AppFinder;
import upc.edu.gessi.repo.service.GraphDBService;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@RestController
public class GraphDBController {

    private Logger logger = LoggerFactory.getLogger(GraphDBController.class);

    @Autowired
    private GraphDBService dbConnection;

    @Autowired
    private AppFinder appFinder;

    @Value("${rml.path}")
    private String rmlPath;

    @GetMapping("/app")
    @ApiIgnore
    public App getData(@RequestParam(value = "app_name", defaultValue = "OsmAnd") String name) {
        App app;
        try {
            app =  appFinder.retrieveAppByName(name.toLowerCase());
            return app;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
            //throw new RuntimeException(e);
        }
    }

    @PostMapping("/app/json")
    @ApiOperation(value = "Insert Data (JSON format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent in JSON format through the request body.")
    public int insertData(@RequestBody List<App> apps) {
        for (App app : apps) {
            dbConnection.insertApp(app);
        }
        return 1;
    }

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

    @PostMapping("/update")
    public int update(@RequestParam(value = "daysFromLastUpdate") int daysFromLastUpdate) {
        dbConnection.updateApps(daysFromLastUpdate);
        return 1;
    }

    @GetMapping("/export")
    public void export(@RequestParam(value = "fileName") String fileName) throws Exception{
        logger.info("Initializing export...");
        dbConnection.exportRepository(fileName);
        logger.info("Repository successfully exported at " + fileName);
    }

    @PostMapping("updateRepository")
    public void updateRepository(@RequestParam(value = "url") String url) {
        logger.info("Updating repo");
        dbConnection.updateRepository(url);
        logger.info("Repository updated");
    }

}
