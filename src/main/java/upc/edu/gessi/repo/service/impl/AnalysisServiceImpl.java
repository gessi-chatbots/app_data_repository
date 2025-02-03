package upc.edu.gessi.repo.service.impl;


import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
@Lazy
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
        dayStatistics.setEmotionOcurrences(new ArrayList<>());
        dayStatistics.setFeatureOccurrences(new ArrayList<>());
        return dayStatistics;
    }

    private void updateSentimentOccurrences(ApplicationDayStatisticsDTO dayStatistics, String sentiment) {
        for (EmotionOccurrenceDTO emotionOccurrence : dayStatistics.getEmotionOcurrences()) {
            if (emotionOccurrence.getEmotion().equals(sentiment)) {
                emotionOccurrence.setOccurrences(emotionOccurrence.getOccurrences() + 1);
                return;
            }
        }
        EmotionOccurrenceDTO newEmotionOccurrence = new EmotionOccurrenceDTO();
        newEmotionOccurrence.setEmotion(sentiment);
        newEmotionOccurrence.setOccurrences(1);
        dayStatistics.getEmotionOcurrences().add(newEmotionOccurrence);
    }

    private void updateFeatureOccurrences(ApplicationDayStatisticsDTO dayStatistics, String feature) {
        for (FeatureOccurrenceDTO featureOccurrence : dayStatistics.getFeatureOccurrences()) {
            if (featureOccurrence.getFeature().equals(feature)) {
                featureOccurrence.setOccurrences(featureOccurrence.getOccurrences() + 1);
                return;
            }
        }
        FeatureOccurrenceDTO newFeatureOccurrence = new FeatureOccurrenceDTO();
        newFeatureOccurrence.setFeature(feature);
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
            featureOccurrenceDTO.setFeature(featureValue);
            featureOccurrenceDTO.setOccurrences(countValue);
        }
        return featureOccurrenceDTO;
    }

    private EmotionOccurrenceDTO getEmotionOccurrence(final TupleQueryResult result) {
        EmotionOccurrenceDTO emotionOccurrenceDTO = new EmotionOccurrenceDTO();
        BindingSet bindings = result.next();
        String emotion = bindings.getBinding("emotion").getValue().stringValue();
        Integer countValue = Integer.valueOf(bindings.getBinding("count").getValue().stringValue());
        emotionOccurrenceDTO.setEmotion(emotion);
        emotionOccurrenceDTO.setOccurrences(countValue);
        return emotionOccurrenceDTO;
    }
    private PolarityOccurrenceDTO getPolarityOccurrence(final TupleQueryResult result) {
        PolarityOccurrenceDTO polarityOccurrenceDTO = new PolarityOccurrenceDTO();
        BindingSet bindings = result.next();
        String polarity = bindings.getBinding("polarityId").getValue().stringValue();
        Integer countValue = Integer.valueOf(bindings.getBinding("count").getValue().stringValue());
        polarityOccurrenceDTO.setPolarity(polarity);
        polarityOccurrenceDTO.setOccurrences(countValue);
        return polarityOccurrenceDTO;
    }

    private TypeOccurrenceDTO getTypeOccurrence(final TupleQueryResult result) {
        TypeOccurrenceDTO typeOccurrenceDTO = new TypeOccurrenceDTO();
        BindingSet bindings = result.next();
        String type = bindings.getBinding("typeId").getValue().stringValue();
        Integer countValue = Integer.valueOf(bindings.getBinding("count").getValue().stringValue());
        typeOccurrenceDTO.setType(type);
        typeOccurrenceDTO.setOccurrences(countValue);
        return typeOccurrenceDTO;
    }

    private TopicOccurrenceDTO getTopicOccurence(final TupleQueryResult result) {
        TopicOccurrenceDTO topicOccurrenceDTO = new TopicOccurrenceDTO();
        BindingSet bindings = result.next();
        String topic = bindings.getBinding("topicId").getValue().stringValue();
        Integer countValue = Integer.valueOf(bindings.getBinding("count").getValue().stringValue());
        topicOccurrenceDTO.setTopic(topic);
        topicOccurrenceDTO.setOccurrences(countValue);
        return topicOccurrenceDTO;
    }

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

    @Override

    public List<ApplicationDayStatisticsDTO> getApplicationStatistics(final String appName,
                                                                      final String descriptor,
                                                                      final Date startDate,
                                                                      final Date endDate) {
        String query = "";
        if ("emotion".equalsIgnoreCase(descriptor)) {
            query = analysisQueryBuilder.findEmotionStatisticBetweenDates(appName, startDate, endDate);
        } else if ("type".equalsIgnoreCase(descriptor)) {
            query = analysisQueryBuilder.findTypeStatisticBetweenDates(appName, startDate, endDate);
        } else if ("topic".equalsIgnoreCase(descriptor)) {
            query = analysisQueryBuilder.findTopicStatisticBetweenDates(appName, startDate, endDate);
        } else if ("polarity".equalsIgnoreCase(descriptor)) {
            query = analysisQueryBuilder.findPolarityStatisticBetweenDates(appName, startDate, endDate);
        } else {
            throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
        }

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
    public TopEmotionsDTO findTopEmotionsByApp(final List<String> appNames){
        String query = analysisQueryBuilder.findTopEmotionsByAppNamesQuery(appNames);
        return getTopEmotionsDTO(query);
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
    public TopFeaturesDTO findTopFeatures() {
        String query = analysisQueryBuilder.findTopFeatures();
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

    private TopEmotionsDTO getTopEmotionsDTO(String query) {
        try (TupleQueryResult result = runSparqlQuery(query)) {
            TopEmotionsDTO topEmotionsDTO = new TopEmotionsDTO();
            List<EmotionOccurrenceDTO> emotions = new ArrayList<>();
            while (result.hasNext()) {
                EmotionOccurrenceDTO emotionOccurrenceDTO = getEmotionOccurrence(result);
                emotions.add(emotionOccurrenceDTO);
            }
            topEmotionsDTO.setTopEmotions(emotions);
            return topEmotionsDTO;
        }
    }

    public TopEmotionsDTO findTopEmotions() {
        String query = analysisQueryBuilder.findTopEmotions();
        return getTopEmotionsDTO(query);
    }


    private TopPolaritiesDTO getTopPolaritiesDTO(String query) {
        try (TupleQueryResult result = runSparqlQuery(query)) {
            TopPolaritiesDTO topPolaritiesDTO = new TopPolaritiesDTO();
            List<PolarityOccurrenceDTO> polarities = new ArrayList<>();
            while (result.hasNext()) {
                PolarityOccurrenceDTO polarityOccurrenceDTO = getPolarityOccurrence(result);
                polarities.add(polarityOccurrenceDTO);
            }
            topPolaritiesDTO.setTopPolarities(polarities);
            return topPolaritiesDTO;
        }
    }

    public TopPolaritiesDTO findTopPolarities() {
        String query = analysisQueryBuilder.findTopPolarites();
        return getTopPolaritiesDTO(query);
    }


    private TopTypesDTO getTopTypesDTO(String query) {
        try (TupleQueryResult result = runSparqlQuery(query)) {
            TopTypesDTO topTypesDTO = new TopTypesDTO();
            List<TypeOccurrenceDTO> types = new ArrayList<>();
            while (result.hasNext()) {
                TypeOccurrenceDTO typeOccurrenceDTO = getTypeOccurrence(result);
                types.add(typeOccurrenceDTO);
            }
            topTypesDTO.setTopTypes(types);
            return topTypesDTO;
        }
    }

    public TopTypesDTO findTopTypes() {
        String query = analysisQueryBuilder.findTopTypes();
        return getTopTypesDTO(query);
    }

    // --- TOP TOPICS ---

    private TopTopicsDTO getTopTopicsDTO(String query) {
        try (TupleQueryResult result = runSparqlQuery(query)) {
            TopTopicsDTO topTopicsDTO = new TopTopicsDTO();
            List<TopicOccurrenceDTO> topics = new ArrayList<>();
            while (result.hasNext()) {
                TopicOccurrenceDTO topicOccurrenceDTO = getTopicOccurence(result);
                topics.add(topicOccurrenceDTO);
            }
            topTopicsDTO.setTopicOccurrences(topics);
            return topTopicsDTO;
        }
    }

    public TopTopicsDTO findTopTopics() {
        String query = analysisQueryBuilder.findTopTopics();
        return getTopTopicsDTO(query);
    }

    @Override
    public TopDescriptorsDTO findTopDescriptors() {
        TopDescriptorsDTO topDescriptors = new TopDescriptorsDTO();
        topDescriptors.setTopEmotions(findTopEmotions());
        topDescriptors.setTopTopics(findTopTopics());
        topDescriptors.setTopTypes(findTopTypes());
        topDescriptors.setTopPolarities(findTopPolarities());
        return topDescriptors;
    }


}
