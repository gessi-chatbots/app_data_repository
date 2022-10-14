package upc.edu.gessi.repo.service;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.domain.SimilarityAlgorithm;
import upc.edu.gessi.repo.domain.SimilarityApp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimilarityService {

    private Logger logger = LoggerFactory.getLogger(SimilarityService.class);

    @Autowired
    GraphDBService graphDBService;

    public void computeSimilarity(SimilarityAlgorithm algorithm) {
        graphDBService.getAppsWithFeatures();
        //TODO after creating nodes, use algorithm to compute similarity
        //Utils.saveJSONFile(apps, "graph");
        //Utils.convertGraphSchemaToJSON("statements.rj", "graph.json");

    }

    public Map<String, List<SimilarityApp>> getTopKSimilarApps(List<String> apps, int k, DocumentType documentType) {
        Map<String, List<SimilarityApp>> res = new HashMap<>();
        for (String app : apps) {
            List<SimilarityApp> similarApps = graphDBService.getTopKSimilarApps(app, k, documentType);
            res.put("https://schema.org/MobileApplication/" + app, similarApps);
        }
        return res;
    }

    public void computeFeatureSimilarity() {
        List<IRI> features = graphDBService.getAllFeatures();
        int count = 0;
        for (IRI feature : features) {
            graphDBService.connectFeatureWithSynonyms(feature);
            ++count;
            if (count % 100 == 0) logger.info(count + " apps out of " + features.size());
        }
    }
}
