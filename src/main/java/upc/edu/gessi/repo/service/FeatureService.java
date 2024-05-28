package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.Feature;

public interface FeatureService extends CrudService<Feature> {
    void extractFeaturesByDocument(DocumentType documentType, int batchSize);

    int extractFeaturesFromReviews(int batchSize, int from);
}
