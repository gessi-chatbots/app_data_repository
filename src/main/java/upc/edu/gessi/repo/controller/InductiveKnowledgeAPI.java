package upc.edu.gessi.repo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.SimilarityAlgorithm;
import upc.edu.gessi.repo.dto.SimilarityApp;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inductive-knowledge-api")
public interface InductiveKnowledgeAPI {

    @PostMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    void ping();

    @GetMapping("/lastReview")
    int getLastReview();

    @PostMapping("/derivedNLFeatures")
    int derivedNLFeatures(@RequestParam(value = "documentType") DocumentType documentType,
                          @RequestParam(value = "batch-size") Integer batchSize,
                          @RequestParam(value = "from") Integer from);

    @PostMapping("/computeFeatureSimilarity")
    void computeFeatureSimilarity(@RequestParam(defaultValue = "0.5", name = "threshold") double synonymThreshold);

    @DeleteMapping("/deleteFeatureSimilarities")
    void deleteFeatureSimilarities();

    @PostMapping("/computeSimilarity")
    void computeSimilarity(@RequestParam(defaultValue = "JACCARD") SimilarityAlgorithm algorithm);

    @GetMapping("/findSimilarApps")
    Map<String, List<SimilarityApp>> getTopKSimilarApps(@RequestBody List<String> apps,
                                                        @RequestParam Integer k,
                                                        @RequestParam DocumentType documentType);

    @GetMapping("/findAppsByFeature")
    Map<String, List<SimilarityApp>> findAppsByFeature(@RequestBody List<String> features,
                                                       @RequestParam Integer k,
                                                       @RequestParam DocumentType documentType);

    @GetMapping("/findAppsByFeatures")
    List<SimilarityApp> findAppsByFeatures(@RequestBody List<String> features,
                                           @RequestParam Integer k,
                                           @RequestParam DocumentType documentType);
}
