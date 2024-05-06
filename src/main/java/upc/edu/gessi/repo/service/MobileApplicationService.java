package upc.edu.gessi.repo.service;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import upc.edu.gessi.repo.dto.MobileApplicationDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;

import java.util.List;

public interface MobileApplicationService extends CrudService<MobileApplicationDTO>{

    List<MobileApplicationDTO> findAllApplicationNames() throws ApplicationNotFoundException;

    List<GraphApp> getAllApps();

    void addFeatures(MobileApplicationDTO mobileApplicationDTO,
                     IRI sub,
                     List<Statement> statements);
}
