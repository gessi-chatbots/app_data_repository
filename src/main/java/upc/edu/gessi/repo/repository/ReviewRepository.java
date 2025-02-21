package upc.edu.gessi.repo.repository;



import org.eclipse.rdf4j.query.TupleQueryResult;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDescriptorRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDescriptorResponseDTO;
import upc.edu.gessi.repo.dto.Review.SentenceDTO;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;

import java.util.List;


public interface ReviewRepository extends RDFCRUDRepository<ReviewDTO> {
    List<ReviewDTO> findListed(List<String> reviewIds) throws NoReviewsFoundException;

    List<String> getResultsContaining(String text);

    void addSentenceToReview(String reviewId,
                             String sentenceId);

    List<GraphReview> getReviews(String nodeId);

    SentenceDTO getSentenceDTO(TupleQueryResult result);

    List<ReviewDTO> findBatched(final int limit, final int offset);

    List<ReviewDTO> findAllSimplified();

    Integer getCount();

    List<ReviewDTO> getReviewsByAppNameAndIdentifierWithLimit(String appName,
                                                              String appIdentifier,
                                                              Integer limit);

    List<ReviewDescriptorResponseDTO> findByDescriptors(ReviewDescriptorRequestDTO requestDTO,
                                                        Integer page,
                                                        Integer size) throws NoReviewsFoundException;

    Long countByDescriptors(ReviewDescriptorRequestDTO requestDTO) throws NoReviewsFoundException;

}
