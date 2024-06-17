package upc.edu.gessi.repo.repository.impl;

import io.swagger.models.auth.In;
import jdk.jshell.execution.Util;
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
import upc.edu.gessi.repo.dto.Review.FeatureDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.SentenceDTO;
import upc.edu.gessi.repo.dto.Review.SentimentDTO;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;
import upc.edu.gessi.repo.exception.Reviews.ReviewNotFoundException;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.util.ExcelUtils;
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
            ReviewDTO reviewDTO = getReviewDTO(reviewsResult.next());
            reviewDTOs.add(reviewDTO);
        }
        return reviewDTOs;
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



    private ReviewDTO getReviewDTO(final BindingSet bindings) {
        ReviewDTO ReviewDTO = new ReviewDTO();
        if (existsShortReviewBinding(bindings)) {
            if (bindings.getBinding("id") != null && bindings.getBinding("id").getValue() != null) {
                String idValue = bindings.getBinding("id").getValue().stringValue();
                ReviewDTO.setId(idValue);
            }

            if (bindings.getBinding("text") != null && bindings.getBinding("text").getValue() != null) {
                String textValue = bindings.getBinding("text").getValue().stringValue();
                ReviewDTO.setReviewText(textValue);
            }

            if (bindings.getBinding("app_identifier") != null && bindings.getBinding("app_identifier").getValue() != null) {
                String appValue = bindings.getBinding("app_identifier").getValue().stringValue();
                ReviewDTO.setApplicationId(appValue);

            }

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
                && bindings.getBinding("text").getValue() != null;
    }
}
