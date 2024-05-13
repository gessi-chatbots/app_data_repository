package upc.edu.gessi.repo.repository;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.TupleQueryResult;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.SentenceDTO;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;

import java.util.List;


public interface SentenceRepository extends RDFCRUDRepository<SentenceDTO> {

    SentenceDTO getSentenceDTO(TupleQueryResult result);
}
