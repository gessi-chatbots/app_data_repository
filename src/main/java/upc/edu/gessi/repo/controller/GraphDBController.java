package upc.edu.gessi.repo.controller;

import be.ugent.rml.Utils;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping("/app")
    public int insertData(@RequestBody List<App> apps) {
        for (App app : apps) {
            dbConnection.insertApp(app);
        }
        return 1;
    }

    @PostMapping("/app/rml")
    public ResponseEntity<String> insertRML(@RequestBody List<App> apps) {
        try {

            File mappingFile = Utils.getFile(rmlPath);

            for (App app : apps) {

            }

            return new ResponseEntity<>("RML data inserted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error inserting RML data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/app/rdf")
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
