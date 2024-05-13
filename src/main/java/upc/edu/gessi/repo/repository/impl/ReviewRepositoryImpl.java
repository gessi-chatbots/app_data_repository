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
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.Review.FeatureDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.SentenceDTO;
import upc.edu.gessi.repo.dto.Review.SentimentDTO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
    public ReviewDTO findById(String id) throws ReviewNotFoundException {
        try {
            return findListed(List.of(id)).get(0);
        } catch (NoReviewsFoundException nre) {
            throw new ReviewNotFoundException("Review was not found");
        }
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
    public List<ReviewDTO> findListed(List<String> reviewIds) throws NoReviewsFoundException {
        TupleQueryResult reviewsResult = runSparqlQuery(reviewQueryBuilder.findReviewsByIds(reviewIds));
        if (!reviewsResult.hasNext()) {
            throw new NoReviewsFoundException("Any review was found");
        }
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        while (reviewsResult.hasNext()) {
            ReviewDTO reviewDTO = getReviewDTO(reviewsResult);
            reviewDTOs.add(reviewDTO);
        }
        return reviewDTOs;
    }

    @Override
    public ReviewDTO insert(ReviewDTO entity) {
        List<Statement> statements = new ArrayList<>();
        IRI reviewIRI = factory.createIRI(schemaIRI.getReviewIRI() + "/" + entity.getId());
        IRI applicationIRI = factory.createIRI(schemaIRI.getAppIRI() + "/" + entity.getPackageName());
        statements.add(factory.createStatement(applicationIRI, schemaIRI.getReviewsIRI(), reviewIRI));
        statements.add(factory.createStatement(reviewIRI, schemaIRI.getTypeIRI(), schemaIRI.getReviewIRI()));
        statements.add(factory.createStatement(reviewIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(entity.getId())));
        statements.add(factory.createStatement(reviewIRI, schemaIRI.getAuthorIRI(), factory.createLiteral(entity.getAuthor())));
        statements.add(factory.createStatement(reviewIRI, schemaIRI.getReviewRatingIRI(), factory.createLiteral(entity.getRating())));
        commitChanges(statements);

        addReviewSentencesIntoStatements(statements, reviewIRI, entity.getReviewText(), entity.getSentences());
        return entity;
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
    public void addCompleteReviewsToApplication(final MobileApplicationFullDataDTO completeApplicationDataDTO,
                                                final IRI applicationIRI,final List<Statement> statements) {
        for (ReviewDTO r : completeApplicationDataDTO.getReviews()) {
            if (r.getId() != null) {
                IRI reviewIRI = factory.createIRI(schemaIRI.getReviewIRI() + "/" + r.getId());
                if (applicationIRI != null) {
                    statements.add(factory.createStatement(applicationIRI, schemaIRI.getReviewsIRI(), reviewIRI));
                }
                statements.add(factory.createStatement(reviewIRI, schemaIRI.getTypeIRI(), schemaIRI.getReviewIRI()));
                if (r.getRating() != null) {
                    statements.add(factory.createStatement(reviewIRI, schemaIRI.getReviewRatingIRI(), factory.createLiteral(r.getRating())));
                }
                if (r.getDate() != null) {
                    statements.add(factory.createStatement(reviewIRI, schemaIRI.getDatePublishedIRI(), factory.createLiteral(r.getDate())));
                }
                if (r.getAuthor() != null) {
                    statements.add(factory.createStatement(reviewIRI, schemaIRI.getAuthorIRI(), factory.createLiteral(r.getAuthor())));
                }
                if (r.getId() != null) {
                    statements.add(factory.createStatement(reviewIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(r.getId())));
                }
                if (r.getReviewText() != null) {
                    String reviewBody = r.getReviewText();
                    addReviewSentencesIntoStatements(statements, reviewIRI, reviewBody, r.getSentences());
                }
            }
        }
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

    private void commitChanges(final List<Statement> statements) {
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }
    private void addSentimentIntoStatements(final List<Statement> statements,
                                            final SentenceDTO sentenceDTO,
                                            final IRI sentenceIRI) {
        IRI sentimentIRI = factory.createIRI(schemaIRI.getReactActionIRI() + "/" + sentenceDTO.getSentimentData().getSentiment());
        statements.add(factory.createStatement(sentimentIRI, schemaIRI.getTypeIRI(), schemaIRI.getReactActionIRI()));
        statements.add(factory.createStatement(sentenceIRI, schemaIRI.getPotentialActionIRI(), sentimentIRI));
        statements.add(factory.createStatement(sentimentIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(sentenceDTO.getSentimentData().getSentiment())));
    }

    private void addSenteceFeatureIntoStatements(final List<Statement> statements,
                                                 final SentenceDTO sentenceDTO,
                                                 final IRI sentenceIRI,
                                                 final String reviewId) {
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), reviewQueryBuilder.hasReviewFeatures(reviewId));
        if (result.hasNext()) {
            BindingSet set = result.next();
            if (set.getBinding("hasKeyword") != null && set.getBinding("hasKeyword").getValue() != null) {
                boolean hasFeatures = Boolean.parseBoolean(set.getBinding("hasKeyword").getValue().stringValue());
                if (hasFeatures) {
                    Utils.runSparqlUpdateQuery(repository.getConnection(), reviewQueryBuilder.deleteFeaturesFromReview(reviewId));
                }
            }
        }
        addFeatureToSentence(statements, sentenceDTO, sentenceIRI);

        if (sentenceDTO.getFeatureData().getLanguageModel() != null) {
            addFeatureLanguageModelIntoStatements(sentenceDTO.getSentimentData());
        }
    }

    private void addFeatureLanguageModelIntoStatements(final SentimentDTO sentimentDTO) {

    }

    private void addSentenceSentimentIntoStatements(final List<Statement> statements,
                                                    final SentenceDTO sentenceDTO,
                                                    final IRI sentenceIRI,
                                                    final String reviewId) {
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), reviewQueryBuilder.hasReviewSentiments(reviewId));
        if (result.hasNext()) {
            BindingSet set = result.next();
            if (set.getBinding("hasSentiment") != null && set.getBinding("hasSentiment").getValue() != null) {
                boolean hasSentiments = Boolean.parseBoolean(set.getBinding("hasSentiment").getValue().stringValue());
                if (hasSentiments) {
                    Utils.runSparqlUpdateQuery(repository.getConnection(),
                            reviewQueryBuilder.deleteSentimentsFromReview(reviewId));
                }
            }
        }
        addSentimentIntoStatements(statements, sentenceDTO, sentenceIRI);

        if (sentenceDTO.getSentimentData().getLanguageModel() != null) {
            addSentimentLanguageModelIntoStatements(sentenceDTO.getFeatureData());
        }
    }

    private void addSentimentLanguageModelIntoStatements(final FeatureDTO featureDTO) {

    }

    private void addFeatureToSentence(final List<Statement> statements,
                                      final SentenceDTO sentenceDTO,
                                      final IRI sentenceIRI) {
        String feature =  sentenceDTO.getFeatureData().getFeature();
        feature = feature.replace(" ", "_");
        IRI featureIRI = factory.createIRI(schemaIRI.getDefinedTermIRI() + "/" + feature);
        statements.add(factory.createStatement(featureIRI, schemaIRI.getTypeIRI(), schemaIRI.getDefinedTermIRI()));
        statements.add(factory.createStatement(sentenceIRI, schemaIRI.getKeywordIRI(), featureIRI));
        statements.add(factory.createStatement(featureIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(feature)));
        statements.add(factory.createStatement(featureIRI, schemaIRI.getNameIRI(), factory.createLiteral(feature)));
        // statements.add(factory.createStatement(featureIRI, schemaIRI.getNameIRI(), factory.createLiteral(feature)));
    }

    private void addSentenceIntoStatements(final List<Statement> statements,
                                           final SentenceDTO sentenceDTO,
                                           final IRI reviewIRI) {
        IRI sentenceIRI = factory.createIRI(schemaIRI.getReviewBodyIRI() + "/" + sentenceDTO.getId());
        statements.add(factory.createStatement(sentenceIRI, schemaIRI.getTypeIRI(), schemaIRI.getCreativeWorkIRI()));
        statements.add(factory.createStatement(reviewIRI, schemaIRI.getHasPartIRI(), sentenceIRI));
        statements.add(factory.createStatement(sentenceIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(sentenceDTO.getId())));
        String reviewId = String.valueOf(reviewIRI).split(schemaIRI.getReviewIRI().stringValue()+"/")[1];
        if (sentenceDTO.getSentimentData() != null && sentenceDTO.getSentimentData().getSentiment() != null) {
            addSentenceSentimentIntoStatements(statements, sentenceDTO, sentenceIRI, reviewId);
        }
        if (sentenceDTO.getFeatureData() != null && sentenceDTO.getFeatureData().getFeature() != null) {
            addSenteceFeatureIntoStatements(statements, sentenceDTO, sentenceIRI, reviewId);
        }
    }
    private void addReviewSentencesIntoStatements(final List<Statement> statements,
                                                  final IRI reviewIRI,
                                                  final String reviewBody,
                                                  final List<SentenceDTO> sentences) {
        if (reviewBody != null) {
            byte[] reviewBytes = reviewBody.getBytes();
            String encoded_string = new String(reviewBytes, StandardCharsets.UTF_8);
            statements.add(factory.createStatement(reviewIRI, schemaIRI.getReviewBodyIRI(), factory.createLiteral(encoded_string)));
            if (sentences != null) {
                sentences.forEach(sentenceDTO -> {
                    if (sentenceDTO.getId() != null) {
                        addSentenceIntoStatements(statements, sentenceDTO, reviewIRI);
                    }
                });
            }
        }
    }

    private ReviewDTO getReviewDTO(final TupleQueryResult result) {
        ReviewDTO ReviewDTO = new ReviewDTO();
        BindingSet bindings = result.next();
        if (existsShortReviewBinding(bindings)) {
            String idValue = bindings.getBinding("id").getValue().stringValue();
            String textValue = bindings.getBinding("text").getValue().stringValue();
            String appValue = bindings.getBinding("app_identifier").getValue().stringValue();
            ReviewDTO.setId(idValue);
            ReviewDTO.setReviewText(textValue);
            ReviewDTO.setApplicationId(appValue);
            if (bindings.getBinding("date") != null && bindings.getBinding("date").getValue() != null) {
                String dateString = bindings.getBinding("date").getValue().stringValue();
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = dateFormat.parse(dateString);
                    ReviewDTO.setDate(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        ReviewDTO.setSentences(new ArrayList<>());
        TupleQueryResult sentencesResult =
                runSparqlQuery(reviewQueryBuilder.findReviewSentencesEmotions(new ArrayList<>(Collections.singleton(ReviewDTO.getId()))));
        if (sentencesResult.hasNext()) {
            while (sentencesResult.hasNext()) {
                ReviewDTO
                        .getSentences()
                        .add(getSentenceDTO(sentencesResult));
            }
        } else {
            ReviewDTO.setSentences(new ArrayList<>());
        }

        return ReviewDTO;
    }

    private boolean existsShortReviewBinding(BindingSet bindings) {
        return bindings.getBinding("id") != null
                && bindings.getBinding("id").getValue() != null
                && bindings.getBinding("text") != null
                && bindings.getBinding("text").getValue() != null
                && bindings.getBinding("app_identifier") != null
                && bindings.getBinding("app_identifier").getValue() != null;
    }
}
