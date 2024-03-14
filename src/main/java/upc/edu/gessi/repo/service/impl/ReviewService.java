package upc.edu.gessi.repo.service.impl;


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
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.CompleteApplicationDataDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.ReviewRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewResponseDTO;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.NoReviewsFoundException;
import upc.edu.gessi.repo.repository.impl.ReviewRepository;
import upc.edu.gessi.repo.util.ReviewQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HTTPRepository repository;
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;

    private final ReviewQueryBuilder reviewQueryBuilder;

    @Autowired
    public ReviewService(final @org.springframework.beans.factory.annotation.Value("${db.url}") String url,
                         final @org.springframework.beans.factory.annotation.Value("${db.username}") String username,
                         final @org.springframework.beans.factory.annotation.Value("${db.password}") String password,
                         final ReviewRepository reviewRep,
                         final SchemaIRI schIRI,
                         final ReviewQueryBuilder reviewQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        reviewRepository = reviewRep;
        schemaIRI = schIRI;
        reviewQueryBuilder = reviewQB;
    }

    public List findAll(boolean simplified) throws ApplicationNotFoundException {
        return simplified ? reviewRepository.findAllSimplified() : reviewRepository.findAll();
    }
    public List findAllPaginated(final Integer page, final Integer size, final boolean simplified) throws ApplicationNotFoundException {
        return simplified ? reviewRepository.findAllSimplifiedPaginated(page, size) : reviewRepository.findAll();
    }

    public List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        return  (List<ApplicationSimplifiedDTO>) reviewRepository.findAllReviewIDs();
    }

    public List findByName(final String appName) throws ApplicationNotFoundException {
        return reviewRepository.findByApplicationName(appName);
    }

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

    public List<ReviewResponseDTO> getAllReviewsData(List<ReviewRequestDTO> reviews) throws NoReviewsFoundException {
        List<String> reviewIds = reviews.stream()
                .map(ReviewRequestDTO::getReviewId)
                .collect(Collectors.toList());
        TupleQueryResult result = runSparqlQuery(reviewQueryBuilder.findTextReviewsQuery(reviewIds));
        if (!result.hasNext()) {
            throw new NoReviewsFoundException("Any review was found");
        }
        List<ReviewResponseDTO> reviewResponseDTOs = new ArrayList<>();
        while (result.hasNext()) {
            ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO();
            BindingSet bindings = result.next();
            if (existsIdAndText(bindings)) {
                String appIdentifier = bindings.getBinding("app_identifier").getValue().stringValue();
                String idValue = bindings.getBinding("id").getValue().stringValue();
                String textValue = bindings.getBinding("text").getValue().stringValue();
                reviewResponseDTO.setApplicationId(appIdentifier);
                reviewResponseDTO.setReviewId(idValue);
                reviewResponseDTO.setReview(textValue);
            }
            reviewResponseDTOs.add(reviewResponseDTO);
        }
        return reviewResponseDTOs;
    }
    public List<GraphReview> getReviews(String nodeId) {
        List<GraphReview> graphReviews = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select ?review ?rating ?reviewBody where {\n" +
                "    <" + nodeId + "> schema:review ?review .\n" +
                "    ?review schema:reviewRating ?rating ;\n" +
                "            schema:reviewBody ?reviewBody\n" +
                "} ";

        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

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

    public void addReviewsToApplication(final CompleteApplicationDataDTO completeApplicationDataDTO,
                                        final IRI sub,
                                        final List<Statement> statements) {
        for (ReviewDTO r : completeApplicationDataDTO.getReviewDTOS()) {
            IRI review = factory.createIRI(schemaIRI.getReviewIRI() + "/" + r.getId());
            //normalize the text to utf-8 encoding
            String reviewBody = r.getBody();
            if (reviewBody != null) {
                byte[] reviewBytes = reviewBody.getBytes();
                String encoded_string = new String(reviewBytes, StandardCharsets.UTF_8);
                statements.add(factory.createStatement(review, schemaIRI.getReviewBodyIRI(), factory.createLiteral(encoded_string)));
                if (r.getSentences() != null) {
                    r.getSentences().forEach(sentenceDTO -> {
                        if (sentenceDTO.getId() != null) {
                            IRI sentence = factory.createIRI(schemaIRI.getReviewBodyIRI() + "/" + sentenceDTO.getId());
                            statements.add(factory.createStatement(review, schemaIRI.getReviewBodyIRI(), sentence));
                            statements.add(factory.createStatement(sentence, schemaIRI.getReactActionIRI(), factory.createLiteral(sentenceDTO.getSentiment())));
                            statements.add(factory.createStatement(sentence, schemaIRI.getFeaturesIRI(), factory.createLiteral(sentenceDTO.getFeature())));
                        }
                    });
                }
            }

            //normalize the text to utf-8 encoding
             /*String reviewAuthor = r.getUserName();
             byte[] authorBytes = reviewAuthor.getBytes();
             String  encoded_author = new String(authorBytes, StandardCharsets.UTF_8);
             String  ascii_encoded_author = new String(authorBytes, StandardCharsets.US_ASCII);
             String temp = ascii_encoded_author.replaceAll("[^a-zA-Z0-9]","");
             IRI author;
             if (temp.equals("")) {
                 String new_user_name = "User_"+user_map.size();
                 user_map.put(new_user_name,encoded_author);
                 author = factory.createIRI(personIRI  + "/" +  new_user_name);
             } else {
                 author = factory.createIRI(personIRI + "/" + ascii_encoded_author.replaceAll("[^a-zA-Z0-9]", ""));

             }
             statements.add(factory.createStatement(author, nameIRI, factory.createLiteral(encoded_author)));
             statements.add(factory.createStatement(review, authorIRI, author));*/
            //IRI rating = factory.createIRI(reviewRatingIRI)
            statements.add(factory.createStatement(review, schemaIRI.getReviewRatingIRI(), factory.createLiteral(r.getRating())));
            if (r.getPublished() != null) {
                statements.add(factory.createStatement(review, schemaIRI.getDatePublishedIRI(), factory.createLiteral(r.getPublished())));
            }
            statements.add(factory.createStatement(review, schemaIRI.getAuthorIRI(), factory.createLiteral(r.getAuthor())));
            statements.add(factory.createStatement(sub, schemaIRI.getReviewsIRI(), review));
            statements.add(factory.createStatement(review, schemaIRI.getTypeIRI(), schemaIRI.getReviewIRI()));
            statements.add(factory.createStatement(review, schemaIRI.getIdentifierIRI(), factory.createLiteral(r.getId())));
            //statements.add(factory.createStatement(author, typeIRI, personIRI));
        }
    }

    public void addReviews(final List<ReviewResponseDTO> reviewResponseDTOList) {
        List<Statement> statements = new ArrayList<>();
        for (ReviewResponseDTO r : reviewResponseDTOList) {
            IRI review = factory.createIRI(schemaIRI.getReviewIRI() + "/" + r.getReviewId());
            //normalize the text to utf-8 encoding
            String reviewBody = r.getReview();
            if (reviewBody != null) {
                byte[] reviewBytes = reviewBody.getBytes();
                String encoded_string = new String(reviewBytes, StandardCharsets.UTF_8);
                statements.add(factory.createStatement(review, schemaIRI.getReviewBodyIRI(), factory.createLiteral(encoded_string)));
                if (r.getSentences() != null) {
                    r.getSentences().forEach(sentenceDTO -> {
                        if (sentenceDTO.getId() != null) {
                            IRI sentence = factory.createIRI(schemaIRI.getReviewBodyIRI() + "/" + sentenceDTO.getId());
                            statements.add(factory.createStatement(review, schemaIRI.getReviewBodyIRI(), sentence));
                            if (sentenceDTO.getSentiment() != null) {
                                statements.add(factory.createStatement(sentence, schemaIRI.getReactActionIRI(), factory.createLiteral(sentenceDTO.getSentiment())));
                            }
                            if (sentenceDTO.getFeature() != null) {
                                statements.add(factory.createStatement(sentence, schemaIRI.getFeaturesIRI(), factory.createLiteral(sentenceDTO.getFeature())));
                            }
                        }
                    });
                }
            }
        }
        commitChanges(statements);
    }

    private void commitChanges(final List<Statement> statements) {
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }
    private boolean existsIdAndText(BindingSet bindings) {
        return bindings.getBinding("id") != null
                && bindings.getBinding("id").getValue() != null
                && bindings.getBinding("text") != null
                && bindings.getBinding("text").getValue() != null;
    }

    // TODO finish review extension




}
