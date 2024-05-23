package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.SimilarityApp;

import java.util.List;
import java.util.Map;

public interface SimilarityService {
    Map<String, List<SimilarityApp>> getTopKSimilarApps(List<String> apps, int k, DocumentType documentType);

    Map<String, List<SimilarityApp>> findAppsByFeature(List<String> features, Integer k, DocumentType documentType);

    List<SimilarityApp> findAppsByFeatures(List<String> features, Integer k, DocumentType documentType);

    void computeFeatureSimilarity(double synonymThreshold);

    void deleteFeatureSimilarities();
}
