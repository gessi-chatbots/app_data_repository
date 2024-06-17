package upc.edu.gessi.repo.repository;



import upc.edu.gessi.repo.dto.Review.FeatureDTO;

import java.util.List;
import java.util.Map;


public interface FeatureRepository extends RDFCRUDRepository<FeatureDTO> {
    Map<String, Integer> findAllWithOccurrences();

    List<String> findAllDistinct();
}

