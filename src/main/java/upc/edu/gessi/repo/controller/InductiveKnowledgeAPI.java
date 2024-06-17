package upc.edu.gessi.repo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.SimilarityAlgorithm;
import upc.edu.gessi.repo.dto.SimilarityApp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inductive-knowledge")
public interface InductiveKnowledgeAPI extends BaseAPI {

    @GetMapping("/last-review")
    int getLastReview();

    @PostMapping("/derived-nl-features")
    ResponseEntity<String> derivedNLFeatures(@RequestParam(value = "documentType") DocumentType documentType,
                          @RequestParam(value = "batch-size", defaultValue = "0") Integer batchSize,
                          @RequestParam(value = "from", defaultValue = "0") Integer from,
                          @RequestParam(value = "feature-model", defaultValue = "transfeatex") String featureModel);

    @PostMapping("/compute-feature-similarity")
    void computeFeatureSimilarity(@RequestParam(defaultValue = "0.5", name = "threshold") double synonymThreshold);


    @DeleteMapping("/feature-similarities")
    void deleteFeatureSimilarities();

    @PostMapping("/compute-similarity")
    void computeSimilarity(@RequestParam(defaultValue = "JACCARD") SimilarityAlgorithm algorithm);

    @GetMapping("/find-similar-apps")
    Map<String, List<SimilarityApp>> getTopKSimilarApps(@RequestBody List<String> apps,
                                                        @RequestParam Integer k,
                                                        @RequestParam DocumentType documentType);

    @GetMapping("/find-apps-by-feature")
    Map<String, List<SimilarityApp>> findAppsByFeature(@RequestBody List<String> features,
                                                       @RequestParam Integer k,
                                                       @RequestParam DocumentType documentType);

    @GetMapping("/find-apps-by-features")
    List<SimilarityApp> findAppsByFeatures(@RequestBody List<String> features,
                                           @RequestParam Integer k,
                                           @RequestParam DocumentType documentType);
}
