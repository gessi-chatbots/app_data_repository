package upc.edu.gessi.repo.repository;



import upc.edu.gessi.repo.dao.ApplicationPropDocStatisticDAO;
import upc.edu.gessi.repo.dao.SentenceAndFeatureDAO;
import upc.edu.gessi.repo.dto.Review.FeatureDTO;

import java.util.List;
import java.util.Map;


public interface FeatureRepository extends RDFCRUDRepository<FeatureDTO> {
    Map<String, Integer> findAllWithOccurrences();

    List<SentenceAndFeatureDAO> findAllDistinct();

    List<ApplicationPropDocStatisticDAO> findAllApplicationsStatistics();

    List<SentenceAndFeatureDAO> findAllDescriptionDistinctFeaturesWithSentence();

    List<SentenceAndFeatureDAO> findAllSummaryDistinctFeaturesWithSentence();

    List<SentenceAndFeatureDAO> findAllReviewDistinctFeaturesWithSentence();
}

