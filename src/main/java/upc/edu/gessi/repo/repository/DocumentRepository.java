package upc.edu.gessi.repo.repository;



import upc.edu.gessi.repo.dto.DigitalDocumentDTO;

import java.util.List;
import java.util.Map;


public interface DocumentRepository extends RDFCRUDRepository<DigitalDocumentDTO> {
    Map<String, Integer> findAllDocumentTypeFeaturesWithOccurrences(String documentType);

    List<String> findAllDistinctDocumentTypeFeatures(String documentType);
}

