package upc.edu.gessi.repo.service;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

public interface MobileApplicationService extends CrudService<MobileApplicationFullDataDTO>{


    List<GraphApp> getAllApps();

    void addFeatures(MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                     IRI sub,
                     List<Statement> statements);

    List<MobileApplicationBasicDataDTO> getAllBasicData() throws ApplicationNotFoundException;

    List<MobileApplicationBasicDataDTO> getAllBasicDataPaginated(Integer page,
                            Integer size)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException;


}
