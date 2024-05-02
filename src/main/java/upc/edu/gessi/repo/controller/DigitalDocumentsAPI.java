package upc.edu.gessi.repo.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import upc.edu.gessi.repo.dto.DigitalDocumentDTO;

import java.util.List;

@RequestMapping("/digital-documents")
public interface DigitalDocumentsAPI extends CrudAPI<DigitalDocumentDTO> {

    @ApiOperation(value = "Insert Data (JSON format)", notes = "Inserts a list of Digital Documents into the GraphDB. The " +
            "data is sent in JSON format through the request body.")
    @PostMapping(value = "/", produces = "application/json")
    ResponseEntity<String> insertDigitalDocumentsJSONData(@RequestBody List<DigitalDocumentDTO> digitalDocumentDTOS);
}
