package upc.edu.gessi.repo.controller;



import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.exception.MobileApplications.NoMobileApplicationsFoundException;

import java.util.List;

@RequestMapping("/mobile-applications")

public interface MobileApplicationsAPI extends CrudAPI<MobileApplicationFullDataDTO>{


    @PostMapping("/rml")
    @ApiOperation(value = "Insert Data (RML format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is mapped from a JSON file located in {jsonFolder} using the RML mapping file specified in {rml.path} property.")
    ResponseEntity<String> createViaRMLFormat(@RequestParam("jsonFolder") String jsonFolder);

    @PostMapping("/rdf")
    @ApiOperation(value = "Insert Data (RDF format)", notes = "Inserts a list of App entities into the GraphDB. The " +
            "data is sent directly in RDF format through a multipart file in Turtle format (.ttl).")
    ResponseEntity<String> createViaRDFFormat(@RequestParam("file") MultipartFile file);

    @GetMapping(value = "/basic-data", produces = "application/json")
    @ResponseBody
    ResponseEntity<List<MobileApplicationBasicDataDTO>> getAllMobileApplicationsBasicData() throws NoMobileApplicationsFoundException;

    @GetMapping(value = "/basic-data/paginated", produces = "application/json")
    @ResponseBody
    ResponseEntity<List<MobileApplicationBasicDataDTO>> getAllMobileApplicationsBasicDataPaginated(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size)
            throws NoMobileApplicationsFoundException;


    @GetMapping(value = "/{id}/features", produces = "application/json")
    @ResponseBody
    ResponseEntity<List<String>> getMobileApplicationFeatures(@PathVariable String id);

    @PostMapping("/update-repository")
    void updateRepository(@RequestParam(value = "url") String url);


    @GetMapping("/export")
    void export(@RequestParam(value = "fileName") String fileName) throws Exception;


}
