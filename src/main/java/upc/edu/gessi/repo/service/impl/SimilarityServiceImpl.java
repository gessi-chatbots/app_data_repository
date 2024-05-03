package upc.edu.gessi.repo.service.impl;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.SimilarityApp;
import upc.edu.gessi.repo.repository.impl.MobileApplicationRepository;
import upc.edu.gessi.repo.service.SimilarityService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimilarityServiceImpl implements SimilarityService {

    private Logger logger = LoggerFactory.getLogger(SimilarityServiceImpl.class);

    @Autowired
    MobileApplicationRepository mobileApplicationRepository;

    @Autowired
    GraphDBServiceImpl graphDBServiceImpl;

    @Autowired
    FeatureServiceImpl featureServiceImpl;
/*
    public void computeSimilarity(SimilarityAlgorithm algorithm) {
        graphDBService.getAppsWithFeatures();
        //TODO after creating nodes, use algorithm to compute similarity
        //Utils.saveJSONFile(apps, "graph");
        //Utils.convertGraphSchemaToJSON("statements.rj", "graph.json");

    }
*/
    private void mergeSimilarities(List<SimilarityApp> similarApps, List<SimilarityApp> summarySimilarities, int i) {
        for (SimilarityApp app1 : summarySimilarities) {
            SimilarityApp foundApp = null;
            double score = 0.;
            for (SimilarityApp app2 : similarApps) {
                if (app1.getDocumentID().equals(app2.getDocumentID())) {
                    foundApp = app2;
                    score = app1.getScore();
                }
            }
            if (foundApp != null) {
                //TODO fix workaround to deal with 3-d documents
                if (i == 2)
                    foundApp.setScore((foundApp.getScore() + score) / 2);
                else if (i == 3) {
                    foundApp.setScore(foundApp.getScore() * 2 / 3 + score / 3);
                }
            }
            else {
                similarApps.add(app1);
            }
        }
    }

    public Map<String, List<SimilarityApp>> getTopKSimilarApps(List<String> apps, int k, DocumentType documentType) {
        Map<String, List<SimilarityApp>> res = new HashMap<>();
        for (String app : apps) {
            List<SimilarityApp> similarApps;
            if (documentType.equals(DocumentType.ALL)) {
                List<SimilarityApp> descriptionSimilarities = mobileApplicationRepository.getTopKSimilarApps(app, k, DocumentType.DESCRIPTION);
                List<SimilarityApp> summarySimilarities = mobileApplicationRepository.getTopKSimilarApps(app, k, DocumentType.SUMMARY);
                List<SimilarityApp> changelogSimilarities = mobileApplicationRepository.getTopKSimilarApps(app, k, DocumentType.CHANGELOG);

                similarApps = descriptionSimilarities;
                mergeSimilarities(similarApps, summarySimilarities, 2);
                mergeSimilarities(similarApps, changelogSimilarities, 3);
                similarApps = similarApps.stream().sorted(Comparator.comparingDouble(SimilarityApp::getScore).reversed())
                        .collect(Collectors.toList()).subList(0, k);

            } else {
                similarApps = mobileApplicationRepository.getTopKSimilarApps(app, k, documentType);
            }
            res.put("https://schema.org/MobileApplication/" + app, similarApps);
        }
        return res;
    }

    public Map<String, List<SimilarityApp>> findAppsByFeature(List<String> features, Integer k, DocumentType documentType) {
        Map<String, List<SimilarityApp>> res = new HashMap<>();
        for (String feature : features) {
            List<SimilarityApp> similarFeatures;
            if (documentType.equals(DocumentType.ALL)) {
                List<SimilarityApp> descriptionSimilarities = featureServiceImpl.getTopKAppsByFeature(feature, k, DocumentType.DESCRIPTION);
                List<SimilarityApp> summarySimilarities = featureServiceImpl.getTopKAppsByFeature(feature, k, DocumentType.SUMMARY);
                List<SimilarityApp> changelogSimilarities = featureServiceImpl.getTopKAppsByFeature(feature, k, DocumentType.CHANGELOG);

                similarFeatures = descriptionSimilarities;
                mergeSimilarities(similarFeatures, summarySimilarities, 2);
                mergeSimilarities(similarFeatures, changelogSimilarities, 3);
                similarFeatures = similarFeatures.stream().sorted(Comparator.comparingDouble(SimilarityApp::getScore).reversed())
                        .collect(Collectors.toList()).subList(0, k);

            } else {
                similarFeatures = featureServiceImpl.getTopKAppsByFeature(feature, k, documentType);
            }
            res.put("https://schema.org/DefinedTerm/" + feature.replace(" ", ""), similarFeatures);
        }
        return res;
    }
    public List<SimilarityApp> findAppsByFeatures(List<String> features, Integer k, DocumentType documentType) {
        Map<String, List<SimilarityApp>> res = findAppsByFeature(features, k, documentType);
        List<SimilarityApp> similarityApps = new ArrayList<>();
        for (List<SimilarityApp> apps : res.values()) {
            for (SimilarityApp app : apps) {
                app.setScore(app.getScore() / features.size());
                SimilarityApp existingApp = similarityApps.stream()
                        .filter(a -> a.getDocumentID().equals(app.getDocumentID())).findFirst().orElse(null);
                if (existingApp == null) {
                    similarityApps.add(app);
                } else {
                    existingApp.setScore(existingApp.getScore() + app.getScore() / features.size());
                }
            }
        }
        return similarityApps;
    }

    public void computeFeatureSimilarity(double synonymThreshold) {
        List<IRI> features = featureServiceImpl.getAllFeatures();
        int count = 0;
        for (IRI feature : features) {
            featureServiceImpl.connectFeatureWithSynonyms(feature, synonymThreshold);
            ++count;
            if (count % 100 == 0) logger.info(count + " apps out of " + features.size());
        }
    }

    public void deleteFeatureSimilarities() {
        graphDBServiceImpl.deleteSameAsRelations();
    }

}
