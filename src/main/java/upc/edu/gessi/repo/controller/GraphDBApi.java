package upc.edu.gessi.repo.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;
import upc.edu.gessi.repo.dto.App;

import java.util.List;

@RestController
@RequestMapping("/graph-db-api")
public interface GraphDBApi {

    @PostMapping("/ping")
    ResponseEntity<String> ping();

    @GetMapping("/app")
    @ApiIgnore
    App getData(@RequestParam(value = "app_name", defaultValue = "OsmAnd") String name);

    @PostMapping("/app/json")
    @ApiOperation(value = "Insert Data (JSON format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent in JSON format through the request body.")
    int insertData(@RequestBody List<App> apps);

    @PostMapping("/app/rml")
    @ApiOperation(value = "Insert Data (RML format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is mapped from a JSON file located in {jsonFolder} using the RML mapping file specified in {rml.path} property.")
    ResponseEntity<String> insertRML(@RequestParam("jsonFolder") String jsonFolder);

    @PostMapping("/app/rdf")
    @ApiOperation(value = "Insert Data (RDF format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent directly in RDF format through a multipart file in Turtle format (.ttl).")
    ResponseEntity<String> insertRDF(@RequestParam("file") MultipartFile file);

    @PostMapping("/update")
    int update(@RequestParam(value = "daysFromLastUpdate") int daysFromLastUpdate);

    @GetMapping("/export")
    void export(@RequestParam(value = "fileName") String fileName) throws Exception;

    @PostMapping("updateRepository")
    void updateRepository(@RequestParam(value = "url") String url);
}
