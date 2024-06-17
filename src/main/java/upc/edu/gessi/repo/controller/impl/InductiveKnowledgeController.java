package upc.edu.gessi.repo.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.controller.InductiveKnowledgeAPI;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.SimilarityAlgorithm;
import upc.edu.gessi.repo.dto.SimilarityApp;
import upc.edu.gessi.repo.service.FeatureService;
import upc.edu.gessi.repo.service.ServiceFactory;
import upc.edu.gessi.repo.service.SimilarityService;
import upc.edu.gessi.repo.service.impl.GraphDBServiceImpl;
import upc.edu.gessi.repo.service.impl.SimilarityServiceImpl;

import java.util.List;
import java.util.Map;

@RestController
public class InductiveKnowledgeController implements InductiveKnowledgeAPI {

    @Autowired
    private GraphDBServiceImpl dbConnection;


    private ServiceFactory serviceFactory;
    private SimilarityServiceImpl similarityServiceImpl;

    private final Logger logger = LoggerFactory.getLogger(InductiveKnowledgeController.class);

    @Autowired
    public InductiveKnowledgeController(final ServiceFactory serviceFact) {
        this.serviceFactory = serviceFact;
    }
    @Override
    public void ping() {
    }


    @Override
    public int getLastReview() {
        return dbConnection.getCount();
    }

    @Override
    public ResponseEntity<String> derivedNLFeatures(final DocumentType documentType,
                                                    final Integer batchSize,
                                                    final Integer from,
                                                    final String featureModel) {
        logger.info("Generating derived deductive knowledge from natural language documents");
        if (documentType.equals(DocumentType.ALL)
                || documentType.equals(DocumentType.DESCRIPTION)) {
            logger.info("Deducting features from " + documentType.getName());
            ((FeatureService) useService(FeatureService.class)).extractFeaturesByDocument(DocumentType.DESCRIPTION, batchSize);
        }
        if (documentType.equals(DocumentType.ALL)
                || documentType.equals(DocumentType.CHANGELOG)) {
            logger.info("Deducting features from changelogs...");
            ((FeatureService) useService(FeatureService.class)).extractFeaturesByDocument(DocumentType.CHANGELOG, batchSize);
        }
        if (documentType.equals(DocumentType.ALL)
                || documentType.equals(DocumentType.SUMMARY)) {
            logger.info("Deducting features from summaries...");
            ((FeatureService) useService(FeatureService.class)).extractFeaturesByDocument(DocumentType.SUMMARY, batchSize);
        }
        if (documentType.equals(DocumentType.ALL)
                || documentType.equals(DocumentType.REVIEWS)) {
            logger.info("Deducting features from reviews...");
            try {
                ((FeatureService) useService(FeatureService.class)).extractFeaturesFromReviews(batchSize, from, featureModel);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * DEDUCTIVE KNOWLEGDE - Similarity between features (materialized in graph)
     */
    @Override
    @PostMapping("computeFeatureSimilarity")
    public void computeFeatureSimilarity(@RequestParam(defaultValue = "0.5", name = "threshold") double synonymThreshold) {
        ((SimilarityService) useService(SimilarityService.class)).computeFeatureSimilarity(synonymThreshold);
    }

    @Override
    @DeleteMapping("deleteFeatureSimilarities")
    public void deleteFeatureSimilarities() {
        ((SimilarityService) useService(SimilarityService.class)).deleteFeatureSimilarities();
    }

    /**
     * INDUCTIVE KNOWLEDGE - Extract/report summary from inductive knowledge
     */

    @Override
    @PostMapping("computeSimilarity")
    public void computeSimilarity(@RequestParam(defaultValue = "JACCARD") SimilarityAlgorithm algorithm) {
        //similarityService.computeSimilarity(algorithm);
    }

    @Override
    @GetMapping("findSimilarApps")
    public Map<String, List<SimilarityApp>> getTopKSimilarApps(@RequestBody List<String> apps,
                                                               @RequestParam Integer k,
                                                               @RequestParam DocumentType documentType) {
        return ((SimilarityService) useService(SimilarityService.class)).getTopKSimilarApps(apps, k, documentType);
    }

    @Override
    @GetMapping("findAppsByFeature")
    public Map<String, List<SimilarityApp>> findAppsByFeature(@RequestBody List<String> features,
                                                              @RequestParam Integer k,
                                                              @RequestParam DocumentType documentType) {
        return ((SimilarityService) useService(SimilarityService.class)).findAppsByFeature(features, k, documentType);
    }

    @Override
    @GetMapping("findAppsByFeatures")
    public List<SimilarityApp> findAppsByFeatures(@RequestBody List<String> features,
                                                  @RequestParam Integer k,
                                                  @RequestParam DocumentType documentType) {
        return ((SimilarityService) useService(SimilarityService.class)).findAppsByFeatures(features, k, documentType);
    }



    private Object useService(Class<?> clazz) {
        return serviceFactory.createService(clazz);
    }


}
