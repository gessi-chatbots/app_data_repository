package upc.edu.gessi.repo.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.graph.GraphEdge;
import upc.edu.gessi.repo.dto.graph.GraphNode;
import upc.edu.gessi.repo.service.InductiveKnowledgeService;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static upc.edu.gessi.repo.util.ExcelUtils.*;

@Service
@Lazy
public class InductiveKnowledgeServiceImpl implements InductiveKnowledgeService {

    private Logger logger = LoggerFactory.getLogger(InductiveKnowledgeServiceImpl.class);

    @Value("${inductive-knowledge-service.url}")
    private String url;
    private void request(StringEntity entity) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(url);
            request.addHeader("Content-Type", "application/json");

            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.error("There was some error");
            }

        } catch (Exception ex) {
            logger.error("There was some error with feature extraction");
        }
    }


    public void addNodes(List<GraphNode> nodeList) {
        try {
            JSONArray array = new JSONArray();
            for (GraphNode node : nodeList) array.put(node);
            StringEntity stringEntity = new StringEntity(array.toString());
            request(stringEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addEdges(List<GraphEdge> edgeList) {
        try {
            JSONArray array = new JSONArray();
            for (GraphEdge edge : edgeList) array.put(edge);
            StringEntity stringEntity = new StringEntity(array.toString());
            request(stringEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public byte[] generateAnalyticalExcel() {
        logger.info("Generating Analytical Excel");
        Workbook workbook = generateExcelSheet();
        // Step 1 #Features Total
        insertTotalFeatures(workbook);
        // Step 2 #Features Distinct
        insertDistinctFeatures(workbook);
        // Step 3 Get all apps
        insertAllApplicationsStatistics(workbook);
        // Step 4 Get all document types
        insertAllDocumentTypesStatistics(workbook);
        return new byte[0];
    }



    private void insertTotalFeatures(Workbook workbook) {
        logger.info("Obtaining #total_features");
        Sheet totalFeaturesSheet = createWorkbookSheet(workbook, "Total Features");
        generateTotalFeaturesHeader(workbook, totalFeaturesSheet);
        Map<String, Integer> totalFeatures = getTotalFeatures();
        Integer rowIndex = 1;
        for (Map.Entry<String, Integer> feature : totalFeatures.entrySet()) {
            String featureName = feature.getKey();
            Integer featureOccurrences = feature.getValue();
            ArrayList<String> featureData = new ArrayList<>();
            featureData.add(featureName);
            featureData.add(String.valueOf(featureOccurrences));
            insertRowInSheet(totalFeaturesSheet, featureData, rowIndex);
            rowIndex++;
        }
    }

    private void generateTotalFeaturesHeader(Workbook workbook, Sheet totalFeaturesSheet) {
        List<String> totalFeaturesTitles = new ArrayList<>();
        totalFeaturesTitles.add("Feature Name");
        totalFeaturesTitles.add("Feature Occurrences");
        insertHeaderRowInSheet(totalFeaturesSheet,
                generateTitleCellStyle(workbook),
                generateTitleArial16Font(workbook),
                totalFeaturesTitles);
    }



    private Map<String, Integer> getTotalFeatures() {
        return new HashMap<>();
    }
    private void insertDistinctFeatures(final Workbook workbook) {
        logger.info("Obtaining #distinct_features");
    }

    private void insertAllApplicationsStatistics(final Workbook workbook) {
        getAllApplicationIdentifiers();
        List<String> applicationIdentifiers = new ArrayList<>();
        applicationIdentifiers.forEach(applicationIdentifier -> {
            getApplicationTotalFeatures();
            getApplicationDistinctFeatures();
        });
    }

    private void getAllApplicationIdentifiers() {
        logger.info("Obtaining all application identifiers");
    }

    private void getApplicationTotalFeatures() {
        String app_identifier = "";
        logger.info("Obtaining #total_features for {}", app_identifier);
    }

    private void getApplicationDistinctFeatures() {
        String app_identifier = "";
        logger.info("Obtaining #distinct_features for {}", app_identifier);
    }

    private void insertAllDocumentTypesStatistics(final Workbook workbook) {

    }

    private void getAllDocumentTypes() {
        logger.info("Obtaining all document types");
        logger.info("Obtained the following document types");
    }

}
