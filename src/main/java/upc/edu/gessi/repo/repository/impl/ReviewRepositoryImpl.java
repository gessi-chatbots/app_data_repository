package upc.edu.gessi.repo.repository.impl;

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
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.LanguageModel.LanguageModelDTO;
import upc.edu.gessi.repo.dto.Review.*;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;
import upc.edu.gessi.repo.exception.Reviews.ReviewNotFoundException;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.util.ReviewQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {
    private final HTTPRepository repository;
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;

    private final ReviewQueryBuilder reviewQueryBuilder;

    @Autowired
    public ReviewRepositoryImpl(final @org.springframework.beans.factory.annotation.Value("${db.url}") String url,
                             final @org.springframework.beans.factory.annotation.Value("${db.username}") String username,
                             final @org.springframework.beans.factory.annotation.Value("${db.password}") String password,
                             final SchemaIRI schIRI,
                             final ReviewQueryBuilder reviewQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        schemaIRI = schIRI;
        reviewQueryBuilder = reviewQB;
    }


    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

    @Override
    public ReviewDTO findById(String id) throws NoReviewsFoundException {
        TupleQueryResult reviewsResult = runSparqlQuery(reviewQueryBuilder.findById(id));
        ReviewDTO dto;

        // Check if the review exists
        if (!reviewsResult.hasNext()) {
            throw new NoReviewsFoundException("No Mobile Applications Reviews were found");
        }

        // Process review information
        dto = new ReviewDTO();
        dto.setId(id);

        BindingSet bindings = reviewsResult.next();
        if (bindings.getBinding("app_package") != null) {
            String appPackage = bindings.getBinding("app_package").getValue().stringValue();
            if (!appPackage.isEmpty()) {
                dto.setPackageName(appPackage);
            }
        }

        if (bindings.getBinding("text") != null) {
            String text = bindings.getBinding("text").getValue().stringValue();
            if (!text.isEmpty()) {
                dto.setReviewText(text);
            }
        }

        // Initialize sentences list in ReviewDTO
        dto.setSentences(new ArrayList<>());

        // Retrieve and process sentences details
        TupleQueryResult sentencesResult = runSparqlQuery(
                reviewQueryBuilder.findReviewSentencesWithDetails(dto.getId())
        );

        while (sentencesResult.hasNext()) {
            bindings = sentencesResult.next();

            // Create and initialize SentenceDTO
            SentenceDTO sentenceDTO = new SentenceDTO();
            if (bindings.getBinding("sentenceId") != null) {
                sentenceDTO.setId(bindings.getBinding("sentenceId").getValue().stringValue());
            }

            sentenceDTO.setFeatureData(null);
            sentenceDTO.setPolarityData(null);
            sentenceDTO.setSentimentData(null);
            sentenceDTO.setTypeData(null);
            sentenceDTO.setTopicData(null);

            // Process Features
            if (bindings.getBinding("features") != null) {
                String featuresValue = bindings.getBinding("features").getValue().stringValue();
                if (!featuresValue.isEmpty()) {
                    String[] featuresArray = featuresValue.split(", ");
                    for (String feature : featuresArray) {
                        FeatureDTO featureDTO = new FeatureDTO();
                        featureDTO.setFeature(feature);

                        if (bindings.getBinding("models") != null) {
                            String modelsValue = bindings.getBinding("models").getValue().stringValue();
                            String[] modelsArray = modelsValue.split(", ");
                            if (modelsArray.length > 0) {
                                featureDTO.setLanguageModel(new LanguageModelDTO(modelsArray[0])); // Use the first model
                            }
                        }

                        sentenceDTO.setFeatureData(featureDTO);
                    }
                }
            }

            // Process Emotions
            if (bindings.getBinding("emotions") != null) {
                String emotionsValue = bindings.getBinding("emotions").getValue().stringValue();
                if (!emotionsValue.isEmpty()) {
                    String[] emotionsArray = emotionsValue.split(", ");
                    for (String emotion : emotionsArray) {
                        SentimentDTO sentimentDTO = new SentimentDTO();
                        sentimentDTO.setSentiment(emotion);
                        sentenceDTO.setSentimentData(sentimentDTO);
                    }
                }
            }

            // Process Polarities
            if (bindings.getBinding("polarities") != null) {
                String polaritiesValue = bindings.getBinding("polarities").getValue().stringValue();
                if (!polaritiesValue.isEmpty()) {
                    String[] polaritiesArray = polaritiesValue.split(", ");
                    for (String polarity : polaritiesArray) {
                        PolarityDTO polarityDTO = new PolarityDTO();
                        polarityDTO.setPolarity(polarity);
                        sentenceDTO.setPolarityData(polarityDTO);
                    }
                }
            }

            // Process Types
            if (bindings.getBinding("types") != null) {
                String typesValue = bindings.getBinding("types").getValue().stringValue();
                if (!typesValue.isEmpty()) {
                    String[] typesArray = typesValue.split(", ");
                    for (String type : typesArray) {
                        TypeDTO typeDTO = new TypeDTO();
                        typeDTO.setType(type);
                        sentenceDTO.setTypeData(typeDTO);
                    }
                }
            }

            // Process Topics
            if (bindings.getBinding("topics") != null) {
                String topicsValue = bindings.getBinding("topics").getValue().stringValue();
                if (!topicsValue.isEmpty()) {
                    String[] topicsArray = topicsValue.split(", ");
                    for (String topic : topicsArray) {
                        TopicDTO topicDTO = new TopicDTO();
                        topicDTO.setTopic(topic);
                        sentenceDTO.setTopicData(topicDTO);
                    }
                }
            }

            // Add the sentence to the ReviewDTO
            dto.getSentences().add(sentenceDTO);
        }

        return dto;
    }


    @Override
    public List<ReviewDTO> findAll() throws NoReviewsFoundException {
        List<BindingSet> bindingSetResults = runSparqlQuery(reviewQueryBuilder.findAllQuery()).stream().toList();
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        String currentReviewURI = null;
        ReviewDTO reviewDTO = null;
        for (BindingSet bs : bindingSetResults) {
            String reviewURI = bs.getBinding("subject").getValue().stringValue();
            if (!reviewURI.equals(currentReviewURI)) {
                if (reviewDTO != null) {
                    reviewDTOs.add(reviewDTO);
                }
                currentReviewURI = reviewURI;
                reviewDTO = new ReviewDTO();
            }
            updateReviewDTOFromBindings(reviewDTO, bs);
        }

        if (reviewDTOs.isEmpty()) {
            throw new NoReviewsFoundException("No Mobile Applications Reviews were found");
        }

        return reviewDTOs;
    }


    private void updateReviewDTOFromBindings(ReviewDTO reviewDTO, BindingSet bindings) {
        String predicateValue = bindings.getBinding("predicate").getValue().stringValue();
        String identifierIRI = schemaIRI.getIdentifierIRI().stringValue();
        String authorIRI = schemaIRI.getAuthorIRI().stringValue();
        String reviewBodyIRI = schemaIRI.getReviewBodyIRI().stringValue();
        String reviewRatingIRI = schemaIRI.getReviewRatingIRI().stringValue();
        String value = bindings
                .getBinding("object")
                .getValue()
                .stringValue();
        if (predicateValue.equals(identifierIRI)) {
            reviewDTO.setId(value);
        } else if (predicateValue.equals(authorIRI)) {
            reviewDTO.setAuthor(value);
        } else if (predicateValue.equals(reviewBodyIRI)) {
            reviewDTO.setReviewText(value);
        } else if (predicateValue.equals(reviewRatingIRI)) {
            reviewDTO.setRating(Integer.valueOf(value));
        }
    }


    @Override
    public List<ReviewDTO> findAllPaginated(final Integer page, final Integer size) throws NoReviewsFoundException {
        return null;
    }

    @Override
    public List<ReviewDTO> findListed(final List<String> reviewIds) throws NoReviewsFoundException {
        TupleQueryResult reviewsResult = runSparqlQuery(reviewQueryBuilder.findReviewsByIds(reviewIds));
        if (!reviewsResult.hasNext()) {
            throw new NoReviewsFoundException("No review was found");
        }
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        while (reviewsResult.hasNext()) {
            ReviewDTO reviewDTO = getReviewDTO(reviewsResult.next());
            reviewDTOs.add(reviewDTO);
        }
        return reviewDTOs;
    }


    @Override
    public Long countByDescriptors(ReviewDescriptorRequestDTO requestDTO) throws NoReviewsFoundException {
        TupleQueryResult countResult = runSparqlQuery(reviewQueryBuilder
                .countByDescriptors(requestDTO));

        if (!countResult.hasNext()) {
            throw new NoReviewsFoundException("No review was found");
        }
        Long reviewCount = 0L;
        while (countResult.hasNext()) {
            BindingSet bindings = countResult.next();
            if (bindings.getBinding("totalCount") != null) {
                reviewCount = Long.valueOf(bindings
                        .getBinding("totalCount")
                        .getValue()
                        .stringValue());
                return reviewCount;
            }
        }
        return reviewCount;
    }


    @Override
    public List<ReviewDescriptorResponseDTO> findByDescriptors(ReviewDescriptorRequestDTO requestDTO,
                                                               final Integer page,
                                                               final Integer size) throws NoReviewsFoundException {


        TupleQueryResult reviewsIdsResult = runSparqlQuery(reviewQueryBuilder
                .findReviewsIDsByDescriptors(requestDTO, page, size));

        if (!reviewsIdsResult.hasNext()) {
            throw new NoReviewsFoundException("No review was found");
        }
        List <String> reviewIds = new ArrayList<>();
        while (reviewsIdsResult.hasNext()) {
            BindingSet bindings = reviewsIdsResult.next();
            if (bindings.getBinding("reviewId") != null) {
                String reviewId = bindings.getBinding("reviewId").getValue().stringValue();
                if (!reviewId.isEmpty()) {
                    reviewIds.add(reviewId);
                }
            }
        }

        TupleQueryResult reviewsResult = runSparqlQuery(reviewQueryBuilder
                .findReviewsByIDsDescriptors(reviewIds));

        if (!reviewsResult.hasNext()) {
            throw new NoReviewsFoundException("No review was found");
        }

        List<ReviewDescriptorResponseDTO> reviews = new ArrayList<>();

        while (reviewsResult.hasNext()) {
            BindingSet bindings = reviewsResult.next();
            ReviewDescriptorResponseDTO dto = new ReviewDescriptorResponseDTO();

            dto.setFeatureDTOs(new ArrayList<>());
            dto.setPolarityDTOs(new ArrayList<>());
            dto.setSentimentDTOs(new ArrayList<>());
            dto.setTypeDTOs(new ArrayList<>());
            dto.setTopicDTOs(new ArrayList<>());

            if (bindings.getBinding("reviewId") != null) {
                String reviewId = bindings.getBinding("reviewId").getValue().stringValue();
                if (!reviewId.isEmpty()) {
                    dto.setId(reviewId);
                }
            }

            if (bindings.getBinding("text") != null) {
                String text = bindings.getBinding("text").getValue().stringValue();
                if (!text.isEmpty()) {
                    dto.setReviewText(text);
                }
            }

            if (bindings.getBinding("appId") != null) {
                String appId = bindings.getBinding("appId").getValue().stringValue();
                if (!appId.isEmpty()) {
                    dto.setAppId(appId);
                }
            }

            // Split and add Features
            if (bindings.getBinding("features") != null) {
                String featuresValue = bindings.getBinding("features").getValue().stringValue();
                if (!featuresValue.isEmpty()) {
                    String[] featuresArray = featuresValue.split(", ");
                    for (String feature : featuresArray) {
                        FeatureDTO featureDTO = new FeatureDTO();
                        featureDTO.setFeature(feature);
                        if (bindings.getBinding("models") != null) {
                            String modelsValue = bindings.getBinding("models").getValue().stringValue();
                            String[] modelsArray = modelsValue.split(", ");
                            if (modelsArray.length > 0) {
                                featureDTO.setLanguageModel(new LanguageModelDTO(modelsArray[0])); // Taking the first model
                            }
                        }
                        dto.getFeatureDTOs().add(featureDTO);
                    }
                }
            }

            if (bindings.getBinding("emotions") != null) {
                String emotionsValue = bindings.getBinding("emotions").getValue().stringValue();
                if (!emotionsValue.isEmpty()) {
                    String[] emotionsArray = emotionsValue.split(", ");
                    for (String emotion : emotionsArray) {
                        SentimentDTO sentimentDTO = new SentimentDTO();
                        sentimentDTO.setSentiment(emotion);
                        dto.getSentimentDTOs().add(sentimentDTO);
                    }
                }
            }

            if (bindings.getBinding("polarities") != null) {
                String polaritiesValue = bindings.getBinding("polarities").getValue().stringValue();
                if (!polaritiesValue.isEmpty()) {
                    String[] polaritiesArray = polaritiesValue.split(", ");
                    for (String polarity : polaritiesArray) {
                        PolarityDTO polarityDTO = new PolarityDTO();
                        polarityDTO.setPolarity(polarity);
                        dto.getPolarityDTOs().add(polarityDTO);
                    }
                }
            }

            if (bindings.getBinding("types") != null) {
                String typesValue = bindings.getBinding("types").getValue().stringValue();
                if (!typesValue.isEmpty()) {
                    String[] typesArray = typesValue.split(", ");
                    for (String type : typesArray) {
                        TypeDTO typeDTO = new TypeDTO();
                        typeDTO.setType(type);
                        dto.getTypeDTOs().add(typeDTO);
                    }
                }
            }

            if (bindings.getBinding("topics") != null) {
                String topicsValue = bindings.getBinding("topics").getValue().stringValue();
                if (!topicsValue.isEmpty()) {
                    String[] topicsArray = topicsValue.split(", ");
                    for (String topic : topicsArray) {
                        TopicDTO topicDTO = new TopicDTO();
                        topicDTO.setTopic(topic);
                        dto.getTopicDTOs().add(topicDTO);
                    }
                }
            }

            reviews.add(dto);
        }

        return reviews;
    }


    @Override
    public IRI insert(ReviewDTO dto) {
        List<Statement> statements = new ArrayList<>();
        if (dto.getId() != null) {
            IRI reviewIRI = factory.createIRI(schemaIRI.getReviewIRI() + "/" + dto.getId());
            statements.add(factory.createStatement(reviewIRI, schemaIRI.getTypeIRI(), schemaIRI.getReviewIRI()));
            IRI applicationIRI = null;
            if (dto.getPackageName() != null) {
                applicationIRI = factory.createIRI(schemaIRI.getAppIRI() + "/" + dto.getPackageName());
            }
            if (applicationIRI != null) {
                statements.add(factory.createStatement(applicationIRI, schemaIRI.getReviewsIRI(), reviewIRI));
            }
            if (dto.getRating() != null) {
                statements.add(factory.createStatement(reviewIRI, schemaIRI.getReviewRatingIRI(), factory.createLiteral(dto.getRating())));
            }
            if (dto.getDate() != null) {
                statements.add(factory.createStatement(reviewIRI, schemaIRI.getDatePublishedIRI(), factory.createLiteral(dto.getDate())));
            }
            if (dto.getAuthor() != null) {
                statements.add(factory.createStatement(reviewIRI, schemaIRI.getAuthorIRI(), factory.createLiteral(dto.getAuthor())));
            }
            if (dto.getId() != null) {
                statements.add(factory.createStatement(reviewIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(dto.getId())));
            }
            if (dto.getReviewText() != null) {
                addReviewTextIntoStatements(dto.getReviewText(), statements, reviewIRI);
            }
            commitChanges(statements);
            return reviewIRI;
        }



        // TODO THROW exception
        return null;
    }

    private void addReviewTextIntoStatements(String reviewText, List<Statement> statements, IRI reviewIRI) {
        if (reviewText != null) {
            byte[] reviewBytes = reviewText.getBytes();
            String encoded_string = new String(reviewBytes, StandardCharsets.UTF_8);
            statements.add(factory.createStatement(reviewIRI, schemaIRI.getReviewBodyIRI(), factory.createLiteral(encoded_string)));
        }
    }

    @Override
    public ReviewDTO update(ReviewDTO entity) {
        return null;
    }


    @Override
    public void delete(String id) {
        Utils.runSparqlUpdateQuery(repository.getConnection(),
                reviewQueryBuilder.deleteByIDQuery(id));
    }

    @Override
    public void addSentenceToReview(String reviewId, String sentenceId) {
        List<Statement> statements = new ArrayList<>();
        IRI reviewIRI = factory.createIRI(schemaIRI.getReviewIRI() + "/" + reviewId);
        IRI sentenceIRI = factory.createIRI(schemaIRI.getReviewBodyIRI() + "/" + sentenceId);
        statements.add(factory.createStatement(reviewIRI, schemaIRI.getAdditionalPropertyIRI(), sentenceIRI));
        commitChanges(statements);
    }



    @Override
    public List<String> getResultsContaining(String text) {
        RepositoryConnection repoConnection = repository.getConnection();
        String query = "PREFIX gessi: <http://gessi.upc.edu/app/> SELECT ?x ?y ?z " +
                "WHERE {?x ?y ?z .FILTER regex(str(?z), \""+text+"\")}" ;
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        List<String> resultList = new ArrayList<>();
        TupleQueryResult result = tupleQuery.evaluate();
        while (result.hasNext()) {  // iterate over the result
            BindingSet bindingSet = result.next();
            Value valueOfX = bindingSet.getValue("x");
            Value valueOfY = bindingSet.getValue("y");
            Value valueOfZ = bindingSet.getValue("z");
            resultList.add(valueOfZ.stringValue());
        }
        return resultList;
    }


    @Override
    public List<GraphReview> getReviews(final String nodeId) {
        List<GraphReview> graphReviews = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select ?review ?rating ?reviewBody where {\n" +
                "    <" + nodeId + "> schema:review ?review .\n" +
                "    ?review schema:reviewRating ?rating ;\n" +
                "            schema:reviewBody ?reviewBody\n" +
                "} ";

        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI review = (IRI) bindings.getValue("review");
            Integer reviewRating = Integer.valueOf(bindings.getValue("rating").stringValue());
            String reviewBody = bindings.getValue("reviewBody").stringValue();


            GraphReview graphFeature = new GraphReview(review.toString(), reviewRating, reviewBody);
            graphReviews.add(graphFeature);
        }

        return graphReviews;
    }

    @Override
    public SentenceDTO getSentenceDTO(final TupleQueryResult result) {
        SentenceDTO sentenceDTO = new SentenceDTO();
        BindingSet bindings = result.next();
        if (bindings.getBinding("sentenceId") != null) {
            String sentenceId = bindings.getBinding("sentenceId").getValue().stringValue();
            sentenceDTO.setId(sentenceId);
        }
        if (bindings.getBinding("sentimentValue") != null) {
            String sentimentValue = bindings.getBinding("sentimentValue").getValue().stringValue();
            sentenceDTO.setSentimentData(
                    SentimentDTO
                            .builder()
                            .sentiment(sentimentValue)
                            .build());

        }
        if (bindings.getBinding("featureValue") != null) {
            String featureValue = bindings.getBinding("featureValue").getValue().stringValue().replace("_", " ");
            featureValue = featureValue.replace("_", " ");
            sentenceDTO.setFeatureData(
                    FeatureDTO
                            .builder()
                            .feature(featureValue)
                            .build());
        }

        return sentenceDTO;
    }

    @Override
    public List<ReviewDTO> findBatched(int limit, int offset) {
        String query = reviewQueryBuilder.findAllQueryWithLimitOffset(limit, offset);
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);
        List<ReviewDTO> reviewDTOList = new ArrayList<>();
        while (result.hasNext()) {
            reviewDTOList.add(getReviewDTO(result.next()));
        }
        return reviewDTOList;
    }

    @Override
    public List<ReviewDTO> findAllSimplified() {
        String query = reviewQueryBuilder.findAllSimplifiedQuery();
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);
        List<ReviewDTO> reviewDTOList = new ArrayList<>();
        while (result.hasNext()) {
            reviewDTOList.add(getReviewDTO(result.next()));
        }
        return reviewDTOList;
    }


    @Override
    public Integer getCount() {
        String query = reviewQueryBuilder.getCountQuery();
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

        try {
            if (result.hasNext()) {
                BindingSet bindingSet = result.next();
                String countStr = bindingSet.getValue("count").stringValue();
                return Integer.parseInt(countStr);
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            result.close();
        }
    }

    @Override
    public List<ReviewDTO> getReviewsByAppNameAndIdentifierWithLimit(final String appName,
                                                                     final String appIdentifier,
                                                                     final Integer limit) {
        TupleQueryResult result = Utils
                .runSparqlSelectQuery(
                        repository.getConnection(),
                        reviewQueryBuilder.findReviewsByAppNameAndIdentifierWithLimitQuery(
                                appName,
                                appIdentifier,
                                limit));

        List<ReviewDTO> extractedReviews = new ArrayList<>();
        while(result.hasNext()) {
            BindingSet bindings = result.next();
            ReviewDTO review = new ReviewDTO();
            if(bindings.getBinding("id") != null
                    && bindings.getBinding("id").getValue() != null) {
                review.setId(bindings.getBinding("id").getValue().stringValue());
            }
            if(bindings.getBinding("author") != null
                    && bindings.getBinding("author").getValue() != null) {
                review.setAuthor(bindings.getBinding("author").getValue().stringValue());

            }
            if(bindings.getBinding("reviewText") != null
                    && bindings.getBinding("reviewText").getValue() != null) {
                review.setReviewText(bindings.getBinding("reviewText").getValue().stringValue());
            }
            if(bindings.getBinding("date") != null
                    && bindings.getBinding("date").getValue() != null) {
                review.setDate(Utils
                        .convertStringToDate(
                                bindings
                                        .getBinding("date")
                                        .getValue()
                                        .stringValue())
                );
            }
            if(review.getId() != null) {
               extractedReviews.add(review);
            }
        }
        return extractedReviews;
    }


    private void commitChanges(final List<Statement> statements) {
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }

    private ReviewDescriptorResponseDTO getReviewFeatureDTO(final BindingSet bindings) {
        ReviewDescriptorResponseDTO reviewDescriptorResponseDTO = new ReviewDescriptorResponseDTO();
        if (existsShortReviewBinding(bindings)) {
            if (bindings.getBinding("id") != null && bindings.getBinding("id").getValue() != null) {
                String idValue = bindings.getBinding("id").getValue().stringValue();
                reviewDescriptorResponseDTO.setId(idValue);
            }

            if (bindings.getBinding("text") != null && bindings.getBinding("text").getValue() != null) {
                String textValue = bindings.getBinding("text").getValue().stringValue();
                reviewDescriptorResponseDTO.setReviewText(textValue);
            }

        }

        FeatureDTO featureDTO = new FeatureDTO();
        PolarityDTO polarityDTO = new PolarityDTO();
        TypeDTO typeDTO = new TypeDTO();
        TopicDTO topicDTO = new TopicDTO();
        if (bindings.getBinding("feature") != null && bindings.getBinding("feature").getValue() != null) {
            String feature = bindings.getBinding("feature").getValue().stringValue();
            featureDTO.setFeature(feature);
        }
        if (bindings.getBinding("model") != null && bindings.getBinding("model").getValue() != null) {
            String model = bindings.getBinding("model").getValue().stringValue();
            featureDTO.setLanguageModel(new LanguageModelDTO(model));
        }
        if (bindings.getBinding("polarity") != null && bindings.getBinding("polarity").getValue() != null) {
            String polarity = bindings.getBinding("polarity").getValue().stringValue();
            polarityDTO.setPolarity(polarity);
        }
        if (bindings.getBinding("type") != null && bindings.getBinding("type").getValue() != null) {
            String type = bindings.getBinding("type").getValue().stringValue();
            typeDTO.setType(type);
        }
        if (bindings.getBinding("topic") != null && bindings.getBinding("topic").getValue() != null) {
            String topic = bindings.getBinding("topic").getValue().stringValue();
            topicDTO.setTopic(topic);
        }

        reviewDescriptorResponseDTO.setFeatureDTOs(Collections.singletonList(featureDTO));
        reviewDescriptorResponseDTO.setPolarityDTOs(Collections.singletonList(polarityDTO));
        reviewDescriptorResponseDTO.setTypeDTOs(Collections.singletonList(typeDTO));
        reviewDescriptorResponseDTO.setTopicDTOs(Collections.singletonList(topicDTO));

        return reviewDescriptorResponseDTO;
    }


    private ReviewDTO getReviewDTO(final BindingSet bindings) {
        ReviewDTO reviewDTO = new ReviewDTO();

        if (existsShortReviewBinding(bindings)) {
            if (bindings.getBinding("id") != null && bindings.getBinding("id").getValue() != null) {
                String idValue = bindings.getBinding("id").getValue().stringValue();
                reviewDTO.setId(idValue);
            }

            if (bindings.getBinding("text") != null && bindings.getBinding("text").getValue() != null) {
                String textValue = bindings.getBinding("text").getValue().stringValue();
                reviewDTO.setReviewText(textValue);
            }

            if (bindings.getBinding("app_identifier") != null && bindings.getBinding("app_identifier").getValue() != null) {
                String appPackage = bindings.getBinding("app_identifier").getValue().stringValue();
                reviewDTO.setPackageName(appPackage);
                reviewDTO.setApplicationId(appPackage);
            }

            if (bindings.getBinding("date") != null && bindings.getBinding("date").getValue() != null) {
                String dateString = bindings.getBinding("date").getValue().stringValue();

                Date date = parseDate(dateString);
                if (date != null) {
                    reviewDTO.setDate(date);
                } else {
                    System.err.println("Failed to parse date: " + dateString);
                }
            }
        }

        reviewDTO.setSentences(new ArrayList<>());
        TupleQueryResult sentencesResult = runSparqlQuery(
                reviewQueryBuilder.findReviewSentencesEmotions(new ArrayList<>(Collections.singleton(reviewDTO.getId())))
        );

        if (sentencesResult.hasNext()) {
            while (sentencesResult.hasNext()) {
                reviewDTO.getSentences().add(getSentenceDTO(sentencesResult));
            }
        } else {
            reviewDTO.setSentences(new ArrayList<>());
        }

        return reviewDTO;
    }

    private Date parseDate(String dateString) {
        List<String> dateFormats = Arrays.asList(
                "yyyy-MM-dd",
                "EEE MMM dd HH:mm:ss z yyyy",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
        );

        for (String format : dateFormats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
                dateFormat.setLenient(false);
                return dateFormat.parse(dateString);
            } catch (ParseException ignored) {
            }
        }

        System.err.println("Failed to parse date: " + dateString);
        return null; // If no formats match
    }
    private boolean existsShortReviewBinding(BindingSet bindings) {
        return bindings.getBinding("id") != null
                && bindings.getBinding("id").getValue() != null
                && bindings.getBinding("text") != null
                && bindings.getBinding("text").getValue() != null;
    }
}
