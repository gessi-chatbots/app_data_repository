package upc.edu.gessi.repo.service.impl;


import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.Analysis.*;
import upc.edu.gessi.repo.service.AnalysisService;
import upc.edu.gessi.repo.util.AnalysisQueryBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final HTTPRepository repository;


    private final AnalysisQueryBuilder analysisQueryBuilder;

    @Autowired
    public AnalysisServiceImpl(final @org.springframework.beans.factory.annotation.Value("${db.url}") String url,
                               final @org.springframework.beans.factory.annotation.Value("${db.username}") String username,
                               final @org.springframework.beans.factory.annotation.Value("${db.password}") String password,
                               final AnalysisQueryBuilder analysisQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        analysisQueryBuilder = analysisQB;
    }

    private Date extractDate(BindingSet bindingSet) {
        if (bindingSet.getBinding("date") != null && bindingSet.getBinding("date").getValue() != null ) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            try {
                return dateFormat.parse(bindingSet.getBinding("date").getValue().stringValue());
            } catch (ParseException e) {
               return null;
            }
        } else {
            return null;
        }
    }

    private String extractSentiment(BindingSet bindingSet) {
        if (bindingSet.getBinding("sentiment") != null && bindingSet.getBinding("sentiment").getValue() != null ) {
            return bindingSet.getBinding("sentiment").getValue().stringValue();
        } else {
            return null;
        }
    }

    private String extractFeature(BindingSet bindingSet) {
        if (bindingSet.getBinding("feature") != null && bindingSet.getBinding("feature").getValue() != null ) {
            return bindingSet.getBinding("feature").getValue().stringValue();
        } else {
            return null;
        }
    }

    private ApplicationDayStatisticsDTO createNewDayStatistics(Date date, HashMap<Date, ApplicationDayStatisticsDTO> statisticsMap) {
        ApplicationDayStatisticsDTO dayStatistics = new ApplicationDayStatisticsDTO();
        dayStatistics.setDate(date);
        dayStatistics.setSentimentOccurrences(new ArrayList<>());
        dayStatistics.setFeatureOccurrences(new ArrayList<>());
        return dayStatistics;
    }

    private void updateSentimentOccurrences(ApplicationDayStatisticsDTO dayStatistics, String sentiment) {
        for (SentimentOccurrenceDTO sentimentOccurrence : dayStatistics.getSentimentOccurrences()) {
            if (sentimentOccurrence.getSentimentName().equals(sentiment)) {
                sentimentOccurrence.setOccurrences(sentimentOccurrence.getOccurrences() + 1);
                return;
            }
        }
        SentimentOccurrenceDTO newSentimentOccurrence = new SentimentOccurrenceDTO();
        newSentimentOccurrence.setSentimentName(sentiment);
        newSentimentOccurrence.setOccurrences(1);
        dayStatistics.getSentimentOccurrences().add(newSentimentOccurrence);
    }

    private void updateFeatureOccurrences(ApplicationDayStatisticsDTO dayStatistics, String feature) {
        for (FeatureOccurrenceDTO featureOccurrence : dayStatistics.getFeatureOccurrences()) {
            if (featureOccurrence.getFeatureName().equals(feature)) {
                featureOccurrence.setOccurrences(featureOccurrence.getOccurrences() + 1);
                return;
            }
        }
        FeatureOccurrenceDTO newFeatureOccurrence = new FeatureOccurrenceDTO();
        newFeatureOccurrence.setFeatureName(feature);
        newFeatureOccurrence.setOccurrences(1);
        dayStatistics.getFeatureOccurrences().add(newFeatureOccurrence);
    }


    private boolean existsFeatureBinding(BindingSet bindings) {
        return bindings.getBinding("feature") != null
                && bindings.getBinding("feature").getValue() != null
                && bindings.getBinding("count") != null
                && bindings.getBinding("count").getValue() != null;
    }

    private String getFeature(final TupleQueryResult result) {
        BindingSet bindings = result.next();
        if (bindings.getBinding("feature") != null
                && bindings.getBinding("feature").getValue() != null) {
            return bindings.getBinding("feature").getValue().stringValue();
        }
        return null;
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

    @Override

    public List<ApplicationDayStatisticsDTO> getApplicationStatistics(final String appName, final Date startDate, final Date endDate) {
        String query = analysisQueryBuilder.findStatisticBetweenDates(appName, startDate, endDate);
        HashMap<Date, ApplicationDayStatisticsDTO> statisticsMap = new HashMap<>();
        TupleQueryResult result = runSparqlQuery(query);
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Date date = extractDate(bindingSet);
            String sentiment = extractSentiment(bindingSet);
            String feature = extractFeature(bindingSet);
            ApplicationDayStatisticsDTO dayStatistics = statisticsMap.getOrDefault(date, createNewDayStatistics(date, statisticsMap));
            if (sentiment != null) {
                updateSentimentOccurrences(dayStatistics, sentiment);
            }
            if (feature != null) {
                updateFeatureOccurrences(dayStatistics, feature);
            }
            statisticsMap.put(date, dayStatistics);
        }
        return new ArrayList<>(statisticsMap.values());
    }

    @Override
    public TopSentimentsDTO findTopSentimentsByApps(final List<String> appNames){
        String query = analysisQueryBuilder.findTopSentimentsByAppNamesQuery(appNames);
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
    @Override
    public TopFeaturesDTO findTopFeaturesByApps(final List<String> appNames) {
        String query = analysisQueryBuilder.findTopFeaturesByAppNamesQuery(appNames);
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
    @Override
    public List<String> findAppFeatures(final String appName) {
        String query = analysisQueryBuilder.findFeaturesByAppName(appName);
        TupleQueryResult result = runSparqlQuery(query);
        List<String> features = new ArrayList<>();
        while (result.hasNext()) {
            String feature = getFeature(result);
            features.add(feature);
        }
        return features;
    }


}
