package upc.edu.gessi.repo.repository;



import org.eclipse.rdf4j.query.TupleQueryResult;
import upc.edu.gessi.repo.dto.Review.SentenceDTO;


public interface SentenceRepository extends RDFCRUDRepository<SentenceDTO> {

    SentenceDTO getSentenceDTO(TupleQueryResult result);
}
