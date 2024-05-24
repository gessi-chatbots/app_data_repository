
package upc.edu.gessi.repo.controller;


import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.SoftwareApplicationDTO;

@RequestMapping("/software-applications")

public interface SoftwareApplicationsAPI extends CrudAPI<SoftwareApplicationDTO>  {

}
