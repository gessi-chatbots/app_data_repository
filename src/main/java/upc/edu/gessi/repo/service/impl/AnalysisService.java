package upc.edu.gessi.repo.service.impl;


import org.apache.commons.text.WordUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.Analysis.FeatureOccurrenceDTO;
import upc.edu.gessi.repo.dto.Analysis.SentimentOccurrenceDTO;
import upc.edu.gessi.repo.dto.Analysis.TopFeaturesDTO;
import upc.edu.gessi.repo.dto.Analysis.TopSentimentsDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.CompleteApplicationDataDTO;
import upc.edu.gessi.repo.dto.Review.*;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.NoReviewsFoundException;
import upc.edu.gessi.repo.repository.impl.ReviewRepository;
import upc.edu.gessi.repo.util.AnalyisisQueryBuilder;
import upc.edu.gessi.repo.util.ReviewQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@Service
public class AnalysisService {

    private final ReviewRepository reviewRepository;
    private final HTTPRepository repository;
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;

    private final AnalyisisQueryBuilder analyisisQueryBuilder;

    @Autowired
    public AnalysisService(final @org.springframework.beans.factory.annotation.Value("${db.url}") String url,
                           final @org.springframework.beans.factory.annotation.Value("${db.username}") String username,
                           final @org.springframework.beans.factory.annotation.Value("${db.password}") String password,
                           final ReviewRepository reviewRep,
                           final SchemaIRI schIRI,
                           final AnalyisisQueryBuilder analysisQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        reviewRepository = reviewRep;
        schemaIRI = schIRI;
        analyisisQueryBuilder = analysisQB;
    }

    public TopFeaturesDTO findTopFeaturesByApps(final List<String> appNames) {
        String query = analyisisQueryBuilder.findTopFeaturesByAppNamesQuery(appNames);
        TupleQueryResult result = runSparqlQuery(query);
        TopFeaturesDTO topFeaturesDTO = new TopFeaturesDTO();
        List<FeatureOccurrenceDTO> features = new ArrayList<>();
        while (result.hasNext()) {
            FeatureOccurrenceDTO featureOccurrenceDTO = getFeatureOccurrence(result);
            features.add(featureOccurrenceDTO);
        }
        topFeaturesDTO.setTopFeatures(features);
        return topFeaturesDTO;
    }

    public TopSentimentsDTO findTopSentimentsByApps(final List<String> appNames){
        String query = analyisisQueryBuilder.findTopSentimentsByAppNamesQuery(appNames);
        TupleQueryResult result = runSparqlQuery(query);
        TopSentimentsDTO topSentimentsDTO = new TopSentimentsDTO();
        List<SentimentOccurrenceDTO> sentiments = new ArrayList<>();
        while (result.hasNext()) {
            SentimentOccurrenceDTO sentimentOccurrenceDTO = getSentimentOccurrence(result);
            sentiments.add(sentimentOccurrenceDTO);
        }
        topSentimentsDTO.setTopSentiments(sentiments);
        return topSentimentsDTO;
    }
    private boolean existsFeatureBinding(BindingSet bindings) {
        return bindings.getBinding("feature") != null
                && bindings.getBinding("feature").getValue() != null
                && bindings.getBinding("count") != null
                && bindings.getBinding("count").getValue() != null;
    }
    private FeatureOccurrenceDTO getFeatureOccurrence(final TupleQueryResult result) {
        FeatureOccurrenceDTO featureOccurrenceDTO = new FeatureOccurrenceDTO();
        BindingSet bindings = result.next();
        if (existsFeatureBinding(bindings)) {
            String featureValue = bindings.getBinding("feature").getValue().stringValue();
            Integer countValue = Integer.valueOf(bindings.getBinding("count").getValue().stringValue());
            featureOccurrenceDTO.setFeatureName(featureValue);
            featureOccurrenceDTO.setOccurrences(countValue);
        }
        return featureOccurrenceDTO;
    }

    private boolean existsSentimentBinding(BindingSet bindings) {
        return bindings.getBinding("sentiment") != null
                && bindings.getBinding("sentiment").getValue() != null
                && bindings.getBinding("count") != null
                && bindings.getBinding("count").getValue() != null;
    }
    private SentimentOccurrenceDTO getSentimentOccurrence(final TupleQueryResult result) {
        SentimentOccurrenceDTO sentimentOccurrenceDTO = new SentimentOccurrenceDTO();
        BindingSet bindings = result.next();
        if (existsSentimentBinding(bindings)) {
            String sentimentValue = bindings.getBinding("sentiment").getValue().stringValue();
            Integer countValue = Integer.valueOf(bindings.getBinding("count").getValue().stringValue());
            sentimentOccurrenceDTO.setSentimentName(sentimentValue);
            sentimentOccurrenceDTO.setOccurrences(countValue);
        }

        return sentimentOccurrenceDTO;
    }
    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }




}
