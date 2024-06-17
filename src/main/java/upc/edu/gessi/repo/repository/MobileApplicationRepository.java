package upc.edu.gessi.repo.repository;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.MobileApplications.NoMobileApplicationsFoundException;

import java.util.List;
import java.util.Map;

public interface MobileApplicationRepository extends RDFCRUDRepository<MobileApplicationFullDataDTO> {
    void addFeature(MobileApplicationFullDataDTO completeApplicationDataDTO,
                    IRI sub,
                    List<Statement> statements);

    void addReviewToMobileApplication(String packageName,
                                      String reviewId);

    List<GraphApp> getAllApps();

    List<MobileApplicationBasicDataDTO> findAllBasicDataPaginated(Integer page, Integer size) throws NoMobileApplicationsFoundException;

    List<MobileApplicationBasicDataDTO> findAllApplicationsBasicData() throws NoMobileApplicationsFoundException;

    Map<String, Integer> findAllMobileApplicationFeaturesWithOccurrences(String applicationIdentifier);

    List<String> findAllDistinctMobileApplicationFeatures(String applicationIdentifier);

    List<String> findAllIdentifiers();

    List<MobileApplicationBasicDataDTO> findAllFromMarketSegment(String marketSegment);
}
