package upc.edu.gessi.repo.repository;



import org.eclipse.rdf4j.query.TupleQueryResult;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.SentenceDTO;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;

import java.util.List;


public interface ReviewRepository extends RDFCRUDRepository<ReviewDTO> {
    List<ReviewDTO> findListed(List<String> reviewIds) throws NoReviewsFoundException;

    List<String> getResultsContaining(String text);

    List<GraphReview> getReviews(String nodeId);

    SentenceDTO getSentenceDTO(TupleQueryResult result);
}
