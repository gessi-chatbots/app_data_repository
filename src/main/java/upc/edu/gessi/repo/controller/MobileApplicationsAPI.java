package upc.edu.gessi.repo.controller;



import org.springframework.web.bind.annotation.RequestMapping;
import upc.edu.gessi.repo.dto.ApplicationDataDTO;

@RequestMapping("/mobile-applications")

public interface MobileApplicationsAPI extends CrudAPI<ApplicationDataDTO> {

}
