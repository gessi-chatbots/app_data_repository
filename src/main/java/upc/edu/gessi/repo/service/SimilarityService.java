package upc.edu.gessi.repo.service;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.domain.SimilarityAlgorithm;
import upc.edu.gessi.repo.domain.SimilarityApp;

import javax.print.Doc;
import java.util.*;
import java.util.stream.Collectors;

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
            List<SimilarityApp> similarApps = new ArrayList<>();
            if (documentType.equals(DocumentType.ALL)) {
                List<SimilarityApp> descriptionSimilarities = graphDBService.getTopKSimilarApps(app, k, DocumentType.DESCRIPTION);
                List<SimilarityApp> summarySimilarities = graphDBService.getTopKSimilarApps(app, k, DocumentType.SUMMARY);
                List<SimilarityApp> changelogSimilarities = graphDBService.getTopKSimilarApps(app, k, DocumentType.CHANGELOG);

                similarApps = descriptionSimilarities;
                mergeSimilarities(similarApps, summarySimilarities);
                mergeSimilarities(similarApps, changelogSimilarities);
                similarApps = similarApps.stream().sorted(Comparator.comparingDouble(SimilarityApp::getScore).reversed())
                        .collect(Collectors.toList()).subList(0, k);

            } else {
                similarApps = graphDBService.getTopKSimilarApps(app, k, documentType);
            }
            res.put("https://schema.org/MobileApplication/" + app, similarApps);
        }
        return res;
    }

    private void mergeSimilarities(List<SimilarityApp> similarApps, List<SimilarityApp> summarySimilarities) {
        for (SimilarityApp app1 : summarySimilarities) {
            SimilarityApp foundApp = null;
            double score = 0.;
            for (SimilarityApp app2 : similarApps) {
                if (app1.getDocumentID().equals(app2.getDocumentID())) {
                    foundApp = app2;
                    score = app1.getScore();
                }
            }
            if (foundApp != null) foundApp.setScore(foundApp.getScore() + score);
            else {
                similarApps.add(app1);
            }
        }
    }

    public void computeFeatureSimilarity(double synonymThreshold) {
        List<IRI> features = graphDBService.getAllFeatures();
        int count = 0;
        for (IRI feature : features) {
            graphDBService.connectFeatureWithSynonyms(feature, synonymThreshold);
            ++count;
            if (count % 100 == 0) logger.info(count + " apps out of " + features.size());
        }
    }
}
