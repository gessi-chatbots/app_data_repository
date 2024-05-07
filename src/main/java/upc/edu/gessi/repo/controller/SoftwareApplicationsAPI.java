
package upc.edu.gessi.repo.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.SoftwareApplicationDTO;

import java.util.List;

@RequestMapping("/software-applications")

public interface SoftwareApplicationsAPI extends CrudAPI<SoftwareApplicationDTO>  {

}
