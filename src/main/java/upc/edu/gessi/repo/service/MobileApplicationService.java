package upc.edu.gessi.repo.service;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.MobileApplicationDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

public interface MobileApplicationService {
    List findAll(boolean simplified) throws ApplicationNotFoundException;

    List findAllPaginated(Integer page, Integer size) throws ApplicationNotFoundException;

    List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException;

    MobileApplicationDTO findByName(String appName) throws ObjectNotFoundException;

    List<MobileApplicationDTO> insertApps(List<MobileApplicationDTO> mobileApplicationDTOS);

    List<GraphApp> getAllApps();

    void addFeatures(MobileApplicationDTO mobileApplicationDTO,
                     IRI sub,
                     List<Statement> statements);
}
