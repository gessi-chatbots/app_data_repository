package upc.edu.gessi.repo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.domain.SimilarityAlgorithm;
import upc.edu.gessi.repo.domain.SimilarityApp;
import upc.edu.gessi.repo.service.GraphDBService;
import upc.edu.gessi.repo.service.SimilarityService;

import java.util.List;
import java.util.Map;

@RestController
public class InductiveKnowledgeController {

    @Autowired
    private GraphDBService dbConnection;

    @Autowired
    private SimilarityService similarityService;

    private Logger logger = LoggerFactory.getLogger(InductiveKnowledgeController.class);

    @GetMapping("/getLastReview")
    public int getLastReview() {
        return dbConnection.getCount();
    }

    @PostMapping("/derivedNLFeatures")
    public int derivedNLFeatures(@RequestParam(value = "documentType") DocumentType documentType,
                                 @RequestParam(value = "batch-size") Integer batchSize,
                                 @RequestParam(value = "from") Integer from) {
        logger.info("Generating derived deductive knowledge from natural language documents");
        logger.info("Document type: " + documentType);
        if (documentType.equals(DocumentType.REVIEWS)) {
            logger.info("Deducting features from reviews...");
            try {
                return dbConnection.extractFeaturesFromReviews(batchSize, from);
            } catch (Exception e) {
                return dbConnection.getCount();
            }
        } else if (!documentType.equals(DocumentType.ALL)) {
            logger.info("Deducting features from " + documentType.getName());
            dbConnection.extractFeaturesByDocument(documentType,batchSize);
        } else {
            logger.info("Deducting features from descriptions...");
            dbConnection.extractFeaturesByDocument(DocumentType.DESCRIPTION,batchSize);
            logger.info("Deducting features from changelogs...");
            dbConnection.extractFeaturesByDocument(DocumentType.CHANGELOG,batchSize);
            logger.info("Deducting features from summaries...");
            dbConnection.extractFeaturesByDocument(DocumentType.SUMMARY,batchSize);
            //logger.info("Deducting features from reviews...");
            //dbConnection.extractFeaturesFromReviews();
        }
        return -1;
    }

    /**
     * DEDUCTIVE KNOWLEGDE - Similarity between features (materialized in graph)
     */
    @PostMapping("computeFeatureSimilarity")
    public void computeFeatureSimilarity(@RequestParam(defaultValue = "0.5", name = "threshold") double synonymThreshold) {
        similarityService.computeFeatureSimilarity(synonymThreshold);
    }

    @DeleteMapping("deleteFeatureSimilarities")
    public void deleteFeatureSimilarities() {
        similarityService.deleteFeatureSimilarities();
    }

    /**
     * INDUCTIVE KNOWLEDGE - Extract/report summary from inductive knowledge
     */

    @PostMapping("computeSimilarity")
    public void computeSimilarity(@RequestParam(defaultValue = "JACCARD") SimilarityAlgorithm algorithm) {
        similarityService.computeSimilarity(algorithm);
    }

    @GetMapping("findSimilarApps")
    public Map<String, List<SimilarityApp>> getTopKSimilarApps(@RequestBody List<String> apps,
                                                               @RequestParam Integer k,
                                                               @RequestParam DocumentType documentType) {
        return similarityService.getTopKSimilarApps(apps, k, documentType);
    }

    @GetMapping("findAppsByFeature")
    public Map<String, List<SimilarityApp>> findAppsByFeature(@RequestBody List<String> features,
                                                              @RequestParam Integer k,
                                                              @RequestParam DocumentType documentType) {
        return similarityService.findAppsByFeature(features, k, documentType);
    }

    @GetMapping("findAppsByFeatures")
    public List<SimilarityApp> findAppsByFeatures(@RequestBody List<String> features,
                                                  @RequestParam Integer k,
                                                  @RequestParam DocumentType documentType) {
        return similarityService.findAppsByFeatures(features, k, documentType);
    }

}
