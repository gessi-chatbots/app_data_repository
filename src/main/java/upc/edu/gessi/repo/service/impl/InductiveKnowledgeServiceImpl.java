package upc.edu.gessi.repo.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dao.ApplicationDatesDAO;
import upc.edu.gessi.repo.dao.ApplicationPropDocStatisticDAO;
import upc.edu.gessi.repo.dao.SentenceAndFeatureDAO;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.TermDTO;
import upc.edu.gessi.repo.dto.graph.GraphEdge;
import upc.edu.gessi.repo.dto.graph.GraphNode;
import upc.edu.gessi.repo.repository.*;
import upc.edu.gessi.repo.service.InductiveKnowledgeService;
import upc.edu.gessi.repo.service.ProcessService;
import upc.edu.gessi.repo.util.ExcelUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static upc.edu.gessi.repo.util.ExcelUtils.*;

@Service
@Lazy
public class InductiveKnowledgeServiceImpl implements InductiveKnowledgeService {
    private final RepositoryFactory repositoryFactory;
    private final Logger logger = LoggerFactory.getLogger(InductiveKnowledgeServiceImpl.class);

    private final ProcessService processService;

    private List<SentenceAndFeatureDAO> distinctFeatures = new ArrayList<>();

    private List<TermDTO> top50Nouns = new ArrayList<>();
    private List<TermDTO> top50Verbs = new ArrayList<>();



    @Autowired
    public InductiveKnowledgeServiceImpl(final RepositoryFactory repoFact,
                                         final ProcessService processServ) {
        repositoryFactory = repoFact;
        processService = processServ;
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
        insertSummary(workbook);
        logger.info("Step 3: Inserting all features found in KG");
        insertTotalFeatures(workbook);
        logger.info("Step 4: Obtaining all features along its context");
        obtainFeaturesAndContext();
        logger.info("Step 5: Inserting all applications statistics in KG");
        insertAllApplicationsFeatures(workbook);
        logger.info("Step 6: Inserting all proprietary documents statistics in KG");
        insertAllDocumentTypesStatistics(workbook);
        logger.info("Step 7: Inserting 50 most mentioned verbs & Histogram");
        insert50TopVerbs(workbook);
        logger.info("Step 8: Inserting 50 most mentioned nouns & Histogram");
        insert50TopNouns(workbook);
        logger.info("Step 9: Inserting HeatMap");
        insertHeatMap(workbook);
        logger.info("Step 10: Generating File in Byte[] format");
        return createByteArrayFromWorkbook(workbook);
    }

    private void insertHeatMap(Workbook workbook) {
        Sheet heatMatrixSheet = createWorkbookSheet(workbook, "heatMatrix");
        String heatMatrixContent = processService.executeHeatMapPythonScript(
                distinctFeatures
                        .stream()
                        .map(SentenceAndFeatureDAO::getFeature)
                        .collect(Collectors.toList()),
                top50Verbs,
                top50Nouns);
        if (heatMatrixContent != null) {
            try (BufferedReader reader = new BufferedReader(new StringReader(heatMatrixContent))) {
                String line;
                int rowIndex = 0;

                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    Row row = heatMatrixSheet.createRow(rowIndex++);
                    for (int colIndex = 0; colIndex < values.length; colIndex++) {
                        String valueStr = values[colIndex].trim();
                        if (rowIndex == 1) {
                            Cell headerCell = row.createCell(colIndex + 1);
                            headerCell.setCellValue(valueStr);
                        } else if (colIndex == 0) {
                            Cell headerCell = row.createCell(0);
                            headerCell.setCellValue(valueStr);
                        } else {
                            if (!valueStr.isEmpty()) {
                                try {
                                    double numericValue = Double.parseDouble(valueStr);
                                    Cell cell = row.createCell(colIndex);
                                    cell.setCellValue(numericValue);

                                    SheetConditionalFormatting sheetCF = heatMatrixSheet.getSheetConditionalFormatting();
                                    CellRangeAddress[] regions = {new CellRangeAddress(rowIndex - 1, rowIndex - 1, colIndex, colIndex)};
                                    ConditionalFormattingRule rule = createConditionalFormattingRule(workbook, cell);
                                    sheetCF.addConditionalFormatting(regions, rule);
                                } catch (NumberFormatException e) {
                                    logger.error("Error parsing numeric value: " + valueStr);
                                }
                            }
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                logger.error("Error processing HeatMatrix content: ", e.toString());
                e.printStackTrace();
            }
        } else {
            logger.error("No CSV content returned from Python script");
        }
    }
    private ConditionalFormattingRule createConditionalFormattingRule(Workbook workbook, Cell cell) {
        ConditionalFormattingRule rule = null;
        SheetConditionalFormatting sheetCF = cell.getSheet().getSheetConditionalFormatting();

        try {
            rule = sheetCF.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "0", "100");
            PatternFormatting fill = rule.createPatternFormatting();
            fill.setFillBackgroundColor(getColorBasedOnValue(cell.getNumericCellValue()));
            fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        } catch (Exception e) {
            logger.error("Error creating conditional formatting rule: ", e.toString());
            e.printStackTrace();
        }

        return rule;
    }

    private short getColorBasedOnValue(double value) {
        double normalizedValue = Math.min(Math.max(value, 0), 100);

        double gradientLevel = normalizedValue / 100.0;


        short blueColor;
        if (gradientLevel < 0.25) {
            blueColor = IndexedColors.LIGHT_BLUE.getIndex();
        } else if (gradientLevel < 0.5) {
            blueColor = IndexedColors.SKY_BLUE.getIndex();
        } else if (gradientLevel < 0.75) {
            blueColor = IndexedColors.CORNFLOWER_BLUE.getIndex();
        } else {
            blueColor = IndexedColors.DARK_BLUE.getIndex();
        }

        return blueColor;
    }

    private void insertSummary(final Workbook workbook) {
        Sheet summarySheet = createWorkbookSheet(workbook, "Summary");
        generateSummaryHeader(workbook, summarySheet);
        List<ApplicationPropDocStatisticDAO> appsStatistics = getAllApplicationsSummary();
        Integer rowIndex = 1;

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (ApplicationPropDocStatisticDAO statisticDTO : appsStatistics) {
            ArrayList<String> appData = new ArrayList<>();

            String appIdentifier = statisticDTO.getIdentifier();
            appData.add(appIdentifier);

            String reviewFeatureCount = String.valueOf(statisticDTO.getReviewFeaturesCount());
            appData.add(reviewFeatureCount);

            String summaryFeatureCount = String.valueOf(statisticDTO.getSummaryFeaturesCount());
            appData.add(summaryFeatureCount);

            String descriptionFeatureCount = String.valueOf(statisticDTO.getDescriptionFeaturesCount());
            appData.add(descriptionFeatureCount);

            //TODO fix bug
            String changelogFeatureCount = "";
            appData.add(changelogFeatureCount);

            ApplicationDatesDAO applicationDatesDAO = getApplicationDates(appIdentifier);

            String startDateFormatted = formatDate(applicationDatesDAO.getStartDate(), outputDateFormat);
            appData.add(startDateFormatted);

            String endDateFormatted = formatDate(applicationDatesDAO.getEndDate(), outputDateFormat);
            appData.add(endDateFormatted);

            insertRowInSheet(summarySheet, appData, rowIndex);
            rowIndex++;
        }
    }


    private String formatDate(Date date, SimpleDateFormat outputFormat) {
        if (date == null) {
            return "";
        }
        try {
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public void insert50TopNouns(final Workbook workbook) {
        Sheet top50NounsSheet = createWorkbookSheet(workbook, "Top 50 Nouns");
        generateTop50NounsHeader(workbook, top50NounsSheet);
        //TODO fix script to process the sentence - feature context.
        top50Nouns = processService.executeTop50PythonScript("scripts/top50Nouns.py", distinctFeatures);
        Integer rowIndex = 1;
        for (TermDTO noun : top50Nouns) {
            ArrayList<String> nounData = new ArrayList<>();
            nounData.add(noun.getTerm());
            nounData.add(String.valueOf(noun.getFrequency()));
            insertRowInSheet(top50NounsSheet, nounData, rowIndex);
            rowIndex++;
        }

        XSSFSheet sheet = (XSSFSheet) top50NounsSheet;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 20);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Top 50 Nouns Frequencies");
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Nouns");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Frequency");

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(1, top50Nouns.size(), 0, 0));

        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, top50Nouns.size(), 1, 1));

        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        ((XDDFBarChartData) data).setBarDirection(BarDirection.COL);

        XDDFChartData.Series series = data.addSeries(categories, values);
        series.setTitle("Frequency", null);

        chart.plot(data);
    }
    private void insert50TopVerbs(final Workbook workbook) {
        Sheet top50VerbsSheet = createWorkbookSheet(workbook, "Top 50 Verbs");
        generateTop50VerbsHeader(workbook, top50VerbsSheet);
        top50Verbs = processService.executeTop50PythonScript("scripts/top50Verbs.py", distinctFeatures);
        Integer rowIndex = 1;

        // TODO extract method
        for (TermDTO verb : top50Verbs) {
            ArrayList<String> verbData = new ArrayList<>();
            verbData.add(verb.getTerm());
            verbData.add(String.valueOf(verb.getFrequency()));
            insertRowInSheet(top50VerbsSheet, verbData, rowIndex);
            rowIndex++;
        }

        XSSFSheet sheet = (XSSFSheet) top50VerbsSheet;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 20);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Top 50 Verbs Frequencies");
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Verbs");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Frequency");

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                new CellRangeAddress(1, top50Verbs.size(), 0, 0));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                new CellRangeAddress(1, top50Verbs.size(), 1, 1));

        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        ((XDDFBarChartData) data).setBarDirection(BarDirection.COL);

        XDDFChartData.Series series = data.addSeries(categories, values);
        series.setTitle("Verb distribution", null);

        chart.plot(data);
    }



    private void insertTotalFeatures(Workbook workbook) {
        logger.info("Obtaining #total_features");
        Sheet totalFeaturesSheet = createWorkbookSheet(workbook, "Total Ft.");
        generateTotalFeaturesHeader(workbook, totalFeaturesSheet);
        Map<String, Integer> totalFeatures = getTotalFeatures();
        insertFeaturesAndOcurrencesInSheet(totalFeaturesSheet, totalFeatures);
    }

    private void obtainFeaturesAndContext() {
        logger.info("Obtaining Summary features and context");
        logger.info("Obtaining Review features and context");
        logger.info("Obtaining Description features and context");

        distinctFeatures = getAllDistinctFeatures();


        /*
        Sheet distinctFeaturesSheet = createWorkbookSheet(workbook, "Distinct Ft.");
        generateDistinctFeaturesHeader(workbook, distinctFeaturesSheet);
        Integer rowIndex = 1;
        for (String distinctFeature : distinctFeatures) {
            ArrayList<String> featureData = new ArrayList<>();
            featureData.add(distinctFeature);
            insertRowInSheet(distinctFeaturesSheet, featureData, rowIndex);
            rowIndex++;
        }
        */
    }

    private void generateTotalFeaturesHeader(final Workbook workbook, final Sheet totalFeaturesSheet) {
        List<String> totalFeaturesTitles = new ArrayList<>();
        totalFeaturesTitles.add("Feature Name");
        totalFeaturesTitles.add("Feature Occurrences");
        insertHeaderRowInSheet(totalFeaturesSheet,
                generateTitleCellStyle(workbook),
                generateTitleArial16Font(workbook),
                totalFeaturesTitles);
    }

    private void generateTop50NounsHeader(final Workbook workbook, final Sheet totalFeaturesSheet) {
        List<String> totalFeaturesTitles = new ArrayList<>();
        totalFeaturesTitles.add("Noun");
        totalFeaturesTitles.add("Occurrences");
        insertHeaderRowInSheet(totalFeaturesSheet,
                generateTitleCellStyle(workbook),
                generateTitleArial16Font(workbook),
                totalFeaturesTitles);
    }

    private void generateSummaryHeader(final Workbook workbook, final Sheet statisticsSheet) {
        List<String> statisticsTitle = new ArrayList<>();
        statisticsTitle.add("Application Name");
        statisticsTitle.add("# review features");
        statisticsTitle.add("# Summary features");
        statisticsTitle.add("# Description features");
        statisticsTitle.add("# Changelog features");
        statisticsTitle.add("# Start date");
        statisticsTitle.add("# End date");
        insertHeaderRowInSheet(statisticsSheet,
                generateTitleCellStyle(workbook),
                generateTitleArial16Font(workbook),
                statisticsTitle);
    }

    private void generateTop50VerbsHeader(final Workbook workbook, final Sheet totalFeaturesSheet) {
        List<String> totalFeaturesTitles = new ArrayList<>();
        totalFeaturesTitles.add("Verb");
        totalFeaturesTitles.add("Occurrences");
        insertHeaderRowInSheet(totalFeaturesSheet,
                generateTitleCellStyle(workbook),
                generateTitleArial16Font(workbook),
                totalFeaturesTitles);
    }


    private void insertAllApplicationsFeatures(final Workbook workbook) {
        List<String> applicationIdentifiers = getAllApplicationIdentifiers();
        applicationIdentifiers.forEach(applicationIdentifier -> {
            logger.info("Inserting all application {} features in KG", applicationIdentifier);
            insertTotalApplicationFeatures(workbook, applicationIdentifier);
            //.info("Inserting all application {} distinct features in KG", applicationIdentifier);
            //insertDistinctApplicationFeatures(workbook, applicationIdentifier);
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
            // logger.info("Inserting all Document Type {} distinct features in KG", documentType);
            // insertDistinctDocumentTypeFeatures(workbook, documentType);
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

    private List<SentenceAndFeatureDAO> getAllDistinctFeatures() {
        // Summary
        List<SentenceAndFeatureDAO> sentencesAndFeatures = ((FeatureRepository) useRepository(FeatureRepository.class))
                        .findAllSummaryDistinctFeaturesWithSentence();
        // Description
        sentencesAndFeatures = Stream
                        .concat(
                                sentencesAndFeatures.stream(),
                                ((FeatureRepository) useRepository(FeatureRepository.class))
                                        .findAllDescriptionDistinctFeaturesWithSentence().stream()
                        )
                        .toList();
        // Reviews
        sentencesAndFeatures = Stream
                .concat(
                        sentencesAndFeatures.stream(),
                        ((FeatureRepository) useRepository(FeatureRepository.class))
                                .findAllReviewDistinctFeaturesWithSentence().stream()
                )
                .toList();

        return sentencesAndFeatures;
    }




    private List<ApplicationPropDocStatisticDAO> getAllApplicationsSummary() {
        return ((FeatureRepository) useRepository(FeatureRepository.class)).findAllApplicationsStatistics();
    }


    private List<String> getAllDistinctApplicationFeatures(final String appIdentifier) {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class))
                .findAllDistinctMobileApplicationFeatures(appIdentifier);
    }

    private Map<String, Integer> getTotalApplicationFeatures(final String appIdentifier) {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class))
                .findAllMobileApplicationFeaturesWithOccurrences(appIdentifier);
    }
    private ApplicationDatesDAO getApplicationDates(final String appIdentifier) {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class))
                .getApplicationDates(appIdentifier);
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
