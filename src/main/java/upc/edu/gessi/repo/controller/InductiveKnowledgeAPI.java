package upc.edu.gessi.repo.controller;

import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.SimilarityAlgorithm;
import upc.edu.gessi.repo.dto.SimilarityApp;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inductive-knowledge")
public interface InductiveKnowledgeAPI extends BaseAPI {

    @GetMapping("/last-review")
    int getLastReview();

    @PostMapping("/derived-NL-Features")
    int derivedNLFeatures(@RequestParam(value = "documentType") DocumentType documentType,
                          @RequestParam(value = "batch-size") Integer batchSize,
                          @RequestParam(value = "from") Integer from);

    @PostMapping("/compute-Feature-Similarity")
    void computeFeatureSimilarity(@RequestParam(defaultValue = "0.5", name = "threshold") double synonymThreshold);

    @DeleteMapping("/Feature-Similarities")
    void deleteFeatureSimilarities();

    @PostMapping("/compute-Similarity")
    void computeSimilarity(@RequestParam(defaultValue = "JACCARD") SimilarityAlgorithm algorithm);

    @GetMapping("/find-Similar-Apps")
    Map<String, List<SimilarityApp>> getTopKSimilarApps(@RequestBody List<String> apps,
                                                        @RequestParam Integer k,
                                                        @RequestParam DocumentType documentType);

    @GetMapping("/find-Apps-By-Feature")
    Map<String, List<SimilarityApp>> findAppsByFeature(@RequestBody List<String> features,
                                                       @RequestParam Integer k,
                                                       @RequestParam DocumentType documentType);

    @GetMapping("/find-Apps-By-Features")
    List<SimilarityApp> findAppsByFeatures(@RequestBody List<String> features,
                                           @RequestParam Integer k,
                                           @RequestParam DocumentType documentType);
}
