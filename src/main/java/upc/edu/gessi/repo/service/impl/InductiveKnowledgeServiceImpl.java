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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.graph.GraphEdge;
import upc.edu.gessi.repo.dto.graph.GraphNode;
import upc.edu.gessi.repo.repository.*;
import upc.edu.gessi.repo.service.InductiveKnowledgeService;
import upc.edu.gessi.repo.util.ExcelUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static upc.edu.gessi.repo.util.ExcelUtils.*;

@Service
@Lazy
public class InductiveKnowledgeServiceImpl implements InductiveKnowledgeService {
    private final RepositoryFactory repositoryFactory;
    private Logger logger = LoggerFactory.getLogger(InductiveKnowledgeServiceImpl.class);

    @Autowired
    public InductiveKnowledgeServiceImpl(final RepositoryFactory repoFact) {
        repositoryFactory = repoFact;
    }

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
    public byte[] generateAnalyticalExcel() throws IOException {
        logger.info("Step 1: Generating Analytical Excel");
        Workbook workbook = generateExcelSheet();
        logger.info("Step 2: Inserting summary");
        // TODO
        logger.info("Step 3: Inserting all features found in KG");
        insertTotalFeatures(workbook);
        logger.info("Step 4: Inserting all distinct features found in KG");
        insertDistinctFeatures(workbook);
        logger.info("Step 5: Inserting all applications statistics in KG");
        insertAllApplicationsStatistics(workbook);
        logger.info("Step 6: Inserting all proprietary documents statistics in KG");
        insertAllDocumentTypesStatistics(workbook);
        logger.info("Step 7: Inserting 50 most mentioned terms");
        // TODO
        logger.info("Step 7: Inserting 50 most mentioned verbs");
        // TODO
        logger.info("Step 8: Inserting 50 most mentioned nouns");
        // TODO
        logger.info("Step 9: Generating File in Byte[] format");
        return createByteArrayFromWorkbook(workbook);
    }

    private void insertTotalFeatures(Workbook workbook) {
        logger.info("Obtaining #total_features");
        Sheet totalFeaturesSheet = createWorkbookSheet(workbook, "Total Ft.");
        generateTotalFeaturesHeader(workbook, totalFeaturesSheet);
        Map<String, Integer> totalFeatures = getTotalFeatures();
        insertFeaturesAndOcurrencesInSheet(totalFeaturesSheet, totalFeatures);
    }

    private void insertDistinctFeatures(final Workbook workbook) {
        logger.info("Obtaining #distinct_features");
        Sheet distinctFeaturesSheet = createWorkbookSheet(workbook, "Distinct Ft.");
        generateDistinctFeaturesHeader(workbook, distinctFeaturesSheet);
        List<String> distinctFeatures = getAllDistinctFeatures();
        Integer rowIndex = 1;
        for (String distinctFeature : distinctFeatures) {
            ArrayList<String> featureData = new ArrayList<>();
            featureData.add(distinctFeature);
            insertRowInSheet(distinctFeaturesSheet, featureData, rowIndex);
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

    private void insertAllApplicationsStatistics(final Workbook workbook) {
        List<String> applicationIdentifiers = getAllApplicationIdentifiers();
        applicationIdentifiers.forEach(applicationIdentifier -> {
            logger.info("Inserting all application {} features in KG", applicationIdentifier);
            insertTotalApplicationFeatures(workbook, applicationIdentifier);
            logger.info("Inserting all application {} distinct features in KG", applicationIdentifier);
            insertDistinctApplicationFeatures(workbook, applicationIdentifier);
        });
    }


    private void insertTotalApplicationFeatures(final Workbook workbook, final String applicationIdentifier) {
        logger.info("Obtaining #total_features for {}", applicationIdentifier);
        Sheet totalApplicationFeaturesSheet = workbook.createSheet(
                ExcelUtils.extractLastIdentifierSegment(applicationIdentifier) + " TF");
        generateTotalApplicationFeaturesHeader(workbook, totalApplicationFeaturesSheet);
        Map<String, Integer> totalApplicationFeatures = getTotalApplicationFeatures(applicationIdentifier);
        insertFeaturesAndOcurrencesInSheet(totalApplicationFeaturesSheet, totalApplicationFeatures);
    }

    private void insertTotalDocumentTypeFeatures(final Workbook workbook, final String documentType) {
        logger.info("Obtaining #total_features for {}", documentType);
        Sheet totalDocumentTypeFeaturesSheet = workbook.createSheet("DP. " + documentType + " TF");
        generateTotalApplicationFeaturesHeader(workbook, totalDocumentTypeFeaturesSheet);
        Map<String, Integer> totalDocumentTypeFeatures = getTotalDocumentTypeFeatures(documentType);
        insertFeaturesAndOcurrencesInSheet(totalDocumentTypeFeaturesSheet, totalDocumentTypeFeatures);
    }

    private void insertFeaturesAndOcurrencesInSheet(Sheet totalApplicationFeaturesSheet,
                                                    Map<String, Integer> featuresAndOccurrences) {
        Integer rowIndex = 1;
        for (Map.Entry<String, Integer> feature : featuresAndOccurrences.entrySet()) {
            String featureName = feature.getKey();
            Integer featureOccurrences = feature.getValue();
            ArrayList<String> featureData = new ArrayList<>();
            featureData.add(featureName);
            featureData.add(String.valueOf(featureOccurrences));
            insertRowInSheet(totalApplicationFeaturesSheet, featureData, rowIndex);
            rowIndex++;
        }
    }

    private void generateTotalApplicationFeaturesHeader(final Workbook workbook, final Sheet totalFeaturesSheet) {
        List<String> totalApplicationFeaturesTitles = new ArrayList<>();
        totalApplicationFeaturesTitles.add("Feature Name");
        totalApplicationFeaturesTitles.add("Feature Occurrences");
        insertHeaderRowInSheet(
                totalFeaturesSheet,
                generateTitleCellStyle(workbook),
                generateTitleArial16Font(workbook),
                totalApplicationFeaturesTitles);
    }

    private void insertDistinctApplicationFeatures(final Workbook workbook, final String applicationIdentifier) {
        logger.info("Obtaining #distinct_features for {}", applicationIdentifier);
        Sheet distinctApplicationFeaturesSheet = workbook.createSheet(
                ExcelUtils.extractLastIdentifierSegment(applicationIdentifier) + " DF");
        generateDistinctFeaturesHeader(workbook, distinctApplicationFeaturesSheet);
        List<String> distinctApplicationFeatures = getAllDistinctApplicationFeatures(applicationIdentifier);
        Integer rowIndex = 1;
        for (String distinctFeature : distinctApplicationFeatures) {
            ArrayList<String> featureData = new ArrayList<>();
            featureData.add(distinctFeature);
            insertRowInSheet(distinctApplicationFeaturesSheet, featureData, rowIndex);
            rowIndex++;
        }
    }

    private void insertDistinctDocumentTypeFeatures(final Workbook workbook, final String documentType) {
        logger.info("Obtaining #distinct_features for {}", documentType);
        Sheet distinctApplicationFeaturesSheet = workbook.createSheet("DP." + documentType + " DF");
        generateDistinctFeaturesHeader(workbook, distinctApplicationFeaturesSheet);
        List<String> distinctApplicationFeatures = getAllDistinctDocumentTypeFeatures(documentType);
        Integer rowIndex = 1;
        for (String distinctFeature : distinctApplicationFeatures) {
            ArrayList<String> featureData = new ArrayList<>();
            featureData.add(distinctFeature);
            insertRowInSheet(distinctApplicationFeaturesSheet, featureData, rowIndex);
            rowIndex++;
        }
    }


    private void generateDistinctFeaturesHeader(final Workbook workbook, final Sheet totalFeaturesSheet) {
        List<String> distinctFeaturesTitles = new ArrayList<>();
        distinctFeaturesTitles.add("Feature Name");
        insertHeaderRowInSheet(
                totalFeaturesSheet,
                generateTitleCellStyle(workbook),
                generateTitleArial16Font(workbook),
                distinctFeaturesTitles);
    }



    private void insertAllDocumentTypesStatistics(final Workbook workbook) {
        List<String> documentTypes = getAllDocumentTypes();
        documentTypes.forEach(documentType -> {
            logger.info("Inserting all Document Type {} features in KG", documentType);
            insertTotalDocumentTypeFeatures(workbook, documentType);
            logger.info("Inserting all Document Type {} distinct features in KG", documentType);
            insertDistinctDocumentTypeFeatures(workbook, documentType);
        });
    }



    private List<String> getAllDocumentTypes() {
        logger.info("Obtaining all document types");
        return Arrays.asList(
                DocumentType.DESCRIPTION.getName(),
                DocumentType.SUMMARY.getName(),
                DocumentType.CHANGELOG.getName());
    }
    private List<String> getAllApplicationIdentifiers() {
        logger.info("Obtaining all application identifiers");
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAllIdentifiers();
    }

    private Map<String, Integer> getTotalFeatures() {
        return ((FeatureRepository) useRepository(FeatureRepository.class)).findAllWithOccurrences();
    }

    private List<String> getAllDistinctFeatures() {
        return ((FeatureRepository) useRepository(FeatureRepository.class)).findAllDistinct();
    }

    private List<String> getAllDistinctApplicationFeatures(final String appIdentifier) {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class))
                .findAllDistinctMobileApplicationFeatures(appIdentifier);
    }

    private Map<String, Integer> getTotalApplicationFeatures(final String appIdentifier) {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class))
                .findAllMobileApplicationFeaturesWithOccurrences(appIdentifier);
    }

    private Map<String, Integer> getTotalDocumentTypeFeatures(final String documentType) {
        return ((DocumentRepository) useRepository(DocumentRepository.class))
                .findAllDocumentTypeFeaturesWithOccurrences(documentType);
    }

    private List<String> getAllDistinctDocumentTypeFeatures(final String documentType) {
        return ((DocumentRepository) useRepository(DocumentRepository.class))
                .findAllDistinctDocumentTypeFeatures(documentType);
    }
    private Object useRepository(Class<?> clazz) {
        return repositoryFactory.createRepository(clazz);
    }
}
