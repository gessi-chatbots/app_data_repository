package upc.edu.gessi.repo.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upc.edu.gessi.repo.dto.ApplicationDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/graph-db-api")
public interface GraphDBApi <T> {

    @PostMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    void ping();

    @GetMapping(value = "/applications", params = {"paginated"}, produces = "application/json")
    @ResponseBody
    List<T> getAllApplications(
            @RequestParam(value = "paginated", defaultValue = "false", required = false) boolean paginated,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size,
            @RequestParam(value = "simplified", defaultValue = "true") boolean simplified)
            throws ApplicationNotFoundException, ClassNotFoundException, IllegalAccessException;


    @GetMapping(value = "/applications/names", produces = "application/json")
    @ResponseBody
    List<ApplicationSimplifiedDTO> getAllApplicationsNames() throws ApplicationNotFoundException;

    @GetMapping(value = "/applications/{appName}", produces = "application/json")
    @ResponseBody
    ApplicationDTO getApplicationData(@PathVariable String appName) throws ApplicationNotFoundException, ClassNotFoundException, IllegalAccessException;

    @PostMapping("/app/json")
    @ApiOperation(value = "Insert Data (JSON format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent in JSON format through the request body.")
    int insertData(@RequestBody List<ApplicationDTO> applicationDTOS);

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
