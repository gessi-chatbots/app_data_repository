package upc.edu.gessi.repo.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopSentimentsDTO;
import upc.edu.gessi.repo.dto.ApplicationDataDTO;
import upc.edu.gessi.repo.dto.CompleteApplicationDataDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.Review.ReviewRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewResponseDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.MissingBodyException;
import upc.edu.gessi.repo.exception.NoReviewsFoundException;

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
    ApplicationDataDTO getApplicationData(@PathVariable String appName) throws ApplicationNotFoundException, ClassNotFoundException, IllegalAccessException;

    @PostMapping(value = "/applications", produces = "application/json")
    @ApiOperation(value = "Insert Data (JSON format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent in JSON format through the request body.")
    @ResponseBody
    ResponseEntity<String> insertJSONData(@RequestBody List<CompleteApplicationDataDTO> completeApplicationDataDTOS);

    @PostMapping("/app/rml")
    @ApiOperation(value = "Insert Data (RML format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is mapped from a JSON file located in {jsonFolder} using the RML mapping file specified in {rml.path} property.")
    ResponseEntity<String> insertRML(@RequestParam("jsonFolder") String jsonFolder);

    @PostMapping("/app/rdf")
    @ApiOperation(value = "Insert Data (RDF format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent directly in RDF format through a multipart file in Turtle format (.ttl).")
    ResponseEntity<String> insertRDF(@RequestParam("file") MultipartFile file);

    /*
    @PostMapping("/update")
    int update(@RequestParam(value = "daysFromLastUpdate") int daysFromLastUpdate);
*/
    @GetMapping("/export")
    void export(@RequestParam(value = "fileName") String fileName) throws Exception;

    @PostMapping("updateRepository")
    void updateRepository(@RequestParam(value = "url") String url);

    @GetMapping(value = "/reviews", produces = "application/json")
    @ResponseBody
    List<ReviewResponseDTO> getReviews(@RequestBody List<ReviewRequestDTO> reviews) throws NoReviewsFoundException;

    @GetMapping(value = "/reviews/{reviewId}", produces = "application/json")
    @ResponseBody
    ReviewResponseDTO getReviewData(@PathVariable String reviewId) throws NoReviewsFoundException;


    @PostMapping(value = "/reviews", produces = "application/json")
    @ApiOperation(value = "Insert Data (JSON format)", notes = "Inserts a list of review entities into the GraphDB. The " +
            "data is sent in JSON format through the request body.")
    ResponseEntity<String> insertJSONReviewData(@RequestBody List<ReviewResponseDTO> completeApplicationDataDTOS);


    @PostMapping(value = "/analysis/top-sentiments", produces = "application/json")
    @ResponseBody
    TopSentimentsDTO getTopSentimentsByAppNames(@RequestBody List<String> appNames) throws MissingBodyException;

    @PostMapping(value = "/analysis/top-features", produces = "application/json")
    @ResponseBody
    TopFeaturesDTO getTopFeaturesByAppNames(@RequestBody List<String> appNames) throws MissingBodyException;
}
