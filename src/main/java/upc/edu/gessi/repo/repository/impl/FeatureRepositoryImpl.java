package upc.edu.gessi.repo.repository.impl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dao.ApplicationPropDocStatisticDAO;
import upc.edu.gessi.repo.dao.ReviewDatasetDAO;
import upc.edu.gessi.repo.dao.ReviewSentenceAndFeatureDAO;
import upc.edu.gessi.repo.dao.SentenceAndFeatureDAO;
import upc.edu.gessi.repo.dto.Review.FeatureDTO;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.FeatureRepository;
import upc.edu.gessi.repo.service.ProcessService;
import upc.edu.gessi.repo.util.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class FeatureRepositoryImpl implements FeatureRepository {

    private final HTTPRepository repository;

    private final FeatureQueryBuilder featureQueryBuilder;
    private final ProcessService processService;


    @Autowired
    public FeatureRepositoryImpl(final @Value("${db.url}") String url,
                                 final @Value("${db.username}") String username,
                                 final @Value("${db.password}") String password,
                                 final FeatureQueryBuilder featureQueryBuild,
                                 final ProcessService processSv) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        featureQueryBuilder = featureQueryBuild;
        processService = processSv;


    }


    @Override
    public FeatureDTO findById(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<FeatureDTO> findAll() throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<FeatureDTO> findAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return null;
    }

    @Override
    public IRI insert(FeatureDTO dto) {
        return null;
    }

    @Override
    public FeatureDTO update(FeatureDTO entity) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

    @Override
    public Map<String, Integer> findAllWithOccurrences() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAllFeaturesWithOccurrencesQuery());
        Map<String, Integer> featureOcurrencesDict = new HashMap<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if(bindings.getBinding("identifier") != null
                    && bindings.getBinding("identifier").getValue() != null
                    && bindings.getBinding("totalOccurrences") != null
                    && bindings.getBinding("totalOccurrences").getValue() != null) {
                featureOcurrencesDict.put(
                        bindings.getBinding("identifier").getValue().stringValue(),
                        Integer.valueOf(bindings.getBinding("totalOccurrences").getValue().stringValue())
                );
            }
        }
        return featureOcurrencesDict;
    }

    @Override
    public List<SentenceAndFeatureDAO> findAllDistinct() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAllDistinctFeaturesQuery());
        List<SentenceAndFeatureDAO> featuresList = new ArrayList<>();
        while (result.hasNext()) {
            SentenceAndFeatureDAO sentenceAndFeatureDAO = new SentenceAndFeatureDAO();
            BindingSet bindings = result.next();
            if(bindings.getBinding("feature") != null
                    && bindings.getBinding("feature").getValue() != null) {
                sentenceAndFeatureDAO.setFeature((
                        bindings.getBinding("feature").getValue().stringValue()));
            }
        }
        return featuresList;
    }

    @Override
    public List<ApplicationPropDocStatisticDAO> findAllApplicationsStatistics() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAppStatistics());
        List<ApplicationPropDocStatisticDAO> applicationsStatistics = new ArrayList<>();
        while (result.hasNext()) {
            ApplicationPropDocStatisticDAO applicationPropDocStatisticDAO = new ApplicationPropDocStatisticDAO();
            BindingSet bindings = result.next();

            if(bindings.getBinding("appName") != null
                    && bindings.getBinding("appName").getValue() != null) {
                applicationPropDocStatisticDAO.setIdentifier(bindings.getBinding("appName").getValue().stringValue());
            }

            if(bindings.getBinding("countReviewFeatures") != null
                    && bindings.getBinding("countReviewFeatures").getValue() != null) {
                applicationPropDocStatisticDAO.setReviewFeaturesCount(
                        Integer.valueOf(bindings.getBinding("countReviewFeatures").getValue().stringValue())
                );
            }

            if(bindings.getBinding("countSummaryFeatures") != null
                    && bindings.getBinding("countSummaryFeatures").getValue() != null) {
                applicationPropDocStatisticDAO.setSummaryFeaturesCount(
                        Integer.valueOf(bindings.getBinding("countSummaryFeatures").getValue().stringValue())
                );
            }

            if(bindings.getBinding("countDescriptionFeatures") != null
                    && bindings.getBinding("countDescriptionFeatures").getValue() != null) {
                applicationPropDocStatisticDAO.setDescriptionFeaturesCount(
                        Integer.valueOf(bindings.getBinding("countDescriptionFeatures").getValue().stringValue())
                );
            }
            applicationsStatistics.add(applicationPropDocStatisticDAO);
        }
        return applicationsStatistics;
    }

    @Override
    public List<SentenceAndFeatureDAO> findAllDescriptionDistinctFeaturesWithSentence() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAppDescriptionsFeaturesWithContextQuery());
        List<SentenceAndFeatureDAO> sentenceAndFeatureDAOS = new ArrayList<>();
        while (result.hasNext()) {
            SentenceAndFeatureDAO sentenceAndFeatureDAO = new SentenceAndFeatureDAO();
            BindingSet bindings = result.next();
            if (bindings.getBinding("descriptionText") != null
                    && bindings.getBinding("descriptionText").getValue() != null) {
                sentenceAndFeatureDAO.setSentence(bindings.getBinding("descriptionText").getValue().stringValue());
            }

            if (bindings.getBinding("featureIdentifier") != null
                    && bindings.getBinding("featureIdentifier").getValue() != null) {
                sentenceAndFeatureDAO.setFeature(
                        bindings.getBinding("featureIdentifier").getValue().stringValue()
                );
            }
            sentenceAndFeatureDAOS.add(sentenceAndFeatureDAO);
        }
        return processService.executeExtractSentenceScript(sentenceAndFeatureDAOS);

    }

    @Override
    public List<SentenceAndFeatureDAO> findAllSummaryDistinctFeaturesWithSentence() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAppSummariesFeaturesWithContextQuery());
        List<SentenceAndFeatureDAO> sentenceAndFeatureDAOS = new ArrayList<>();
        while (result.hasNext()) {
            SentenceAndFeatureDAO sentenceAndFeatureDAO = new SentenceAndFeatureDAO();
            BindingSet bindings = result.next();
            if (bindings.getBinding("summaryText") != null
                    && bindings.getBinding("summaryText").getValue() != null) {
                sentenceAndFeatureDAO.setSentence(bindings.getBinding("summaryText").getValue().stringValue());
            }

            if (bindings.getBinding("featureIdentifier") != null
                    && bindings.getBinding("featureIdentifier").getValue() != null) {
                sentenceAndFeatureDAO.setFeature(
                        bindings.getBinding("featureIdentifier").getValue().stringValue()
                );
            }
            sentenceAndFeatureDAOS.add(sentenceAndFeatureDAO);
        }
        return processService.executeExtractSentenceScript(sentenceAndFeatureDAOS);
    }

    @Override
    public List<SentenceAndFeatureDAO> findAllReviewDistinctFeaturesWithSentence() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAppReviewsFeaturesWithContextQuery());
        List<ReviewSentenceAndFeatureDAO> reviewSentenceAndFeatureDAOS = new ArrayList<>();
        while (result.hasNext()) {
            ReviewSentenceAndFeatureDAO reviewSentenceAndFeatureDAO = new ReviewSentenceAndFeatureDAO();
            BindingSet bindings = result.next();
            if (bindings.getBinding("reviewText") != null
                    && bindings.getBinding("reviewText").getValue() != null) {
                reviewSentenceAndFeatureDAO.setSentence(bindings.getBinding("reviewText").getValue().stringValue());
            }

            if (bindings.getBinding("featureIdentifier") != null
                    && bindings.getBinding("featureIdentifier").getValue() != null) {
                reviewSentenceAndFeatureDAO.setFeature(
                        bindings.getBinding("featureIdentifier").getValue().stringValue()
                );
            }

            if (bindings.getBinding("sentenceId") != null
                    && bindings.getBinding("sentenceId").getValue() != null) {
                reviewSentenceAndFeatureDAO.setSentenceId(
                        bindings.getBinding("sentenceId").getValue().stringValue()
                );
            }
            reviewSentenceAndFeatureDAOS.add(reviewSentenceAndFeatureDAO);
        }
        return processService.executeExtractSentenceFromReviewsScript(reviewSentenceAndFeatureDAOS);

    }
    @Override
    public List<ReviewDatasetDAO> findReviews() {
        String csvFilePath = Paths.get("src/main/resources/reviews.csv").toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            writer.write("ApplicationId, ReviewId, ReviewText, Date, TransFeatExFeatures");
            writer.newLine();

            TupleQueryResult result = runSparqlQuery(featureQueryBuilder.featureReviewTextQueryBuilder());
            while (result.hasNext()) {
                BindingSet bindings = result.next();
                if (bindings.getBinding("appIdentifier") != null
                        && bindings.getBinding("appIdentifier").getValue() != null
                        && bindings.getBinding("reviewIdentifier") != null
                        && bindings.getBinding("reviewIdentifier").getValue() != null
                        && bindings.getBinding("reviewText") != null
                        && bindings.getBinding("reviewText").getValue() != null
                        && bindings.getBinding("date") != null
                        && bindings.getBinding("date").getValue() != null
                        && bindings.getBinding("TransFeatexFeatures") != null
                        && bindings.getBinding("TransFeatexFeatures").getValue() != null) {

                    String appIdentifier = escapeCsv(bindings.getBinding("appIdentifier").getValue().stringValue());
                    String reviewIdentifier = escapeCsv(bindings.getBinding("reviewIdentifier").getValue().stringValue());
                    String reviewText = escapeCsv(bindings.getBinding("reviewText").getValue().stringValue());
                    String date = escapeCsv(bindings.getBinding("date").getValue().stringValue());
                    String features = escapeCsv(bindings.getBinding("TransFeatexFeatures").getValue().stringValue());

                    String row = String.format("%s,%s,%s,%s,%s",
                            appIdentifier, reviewIdentifier, reviewText, date, features);
                    writer.write(row);
                    writer.newLine();
                    writer.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV file.");
            e.printStackTrace();
        }

        return null;
    }

    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }

        field = field.replace("\t", " ");

        if (field.contains("\"")) {
            field = field.replace("\"", "\"\"");
        }

        if (field.contains(",") || field.contains("\n") || field.contains("\r") || field.contains(" ")) {
            field = "\"" + field + "\"";
        }

        return field;
    }
}
