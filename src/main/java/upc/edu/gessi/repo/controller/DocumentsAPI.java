package upc.edu.gessi.repo.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import upc.edu.gessi.repo.dto.DigitalDocumentDTO;

import java.util.List;

@RequestMapping("/documents")
public interface DocumentsAPI extends CrudAPI<String> {


}
