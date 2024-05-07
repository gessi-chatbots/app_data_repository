package upc.edu.gessi.repo.repository;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

public interface MobileApplicationRepository extends RDFCRUDRepository<MobileApplicationFullDataDTO> {
    void addFeature(MobileApplicationFullDataDTO completeApplicationDataDTO,
                    IRI sub,
                    List<Statement> statements);

    List<GraphApp> getAllApps();

    List<MobileApplicationBasicDataDTO> findAllBasicDataPaginated(Integer page, Integer size) throws MobileApplicationNotFoundException;

    List<MobileApplicationBasicDataDTO> findAllApplicationsBasicData() throws MobileApplicationNotFoundException;

    MobileApplicationFullDataDTO findByName(String appName) throws ObjectNotFoundException;
}
