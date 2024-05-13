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
import upc.edu.gessi.repo.dto.Review.SentenceDTO;
import upc.edu.gessi.repo.dto.Review.SentenceDTO;
import upc.edu.gessi.repo.dto.Review.SentimentDTO;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;
import upc.edu.gessi.repo.exception.Reviews.ReviewNotFoundException;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.repository.SentenceRepository;
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
public class SentenceRepositoryImpl implements SentenceRepository {
    private final HTTPRepository repository;
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;

    private final ReviewQueryBuilder reviewQueryBuilder;

    @Autowired
    public SentenceRepositoryImpl(final @org.springframework.beans.factory.annotation.Value("${db.url}") String url,
                                  final @org.springframework.beans.factory.annotation.Value("${db.username}") String username,
                                  final @org.springframework.beans.factory.annotation.Value("${db.password}") String password,
                                  final SchemaIRI schIRI,
                                  final ReviewQueryBuilder reviewQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        schemaIRI = schIRI;
        reviewQueryBuilder = reviewQB;
    }


    @Override
    public SentenceDTO findById(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<SentenceDTO> findAll() throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<SentenceDTO> findAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return null;
    }

    @Override
    public IRI insert(SentenceDTO entity) {
        List<Statement> statements = new ArrayList<>();
        IRI sentenceIRI = factory.createIRI(schemaIRI.getReviewBodyIRI() + "/" + entity.getId());
        statements.add(factory.createStatement(sentenceIRI, schemaIRI.getTypeIRI(), schemaIRI.getReviewIRI()));
        statements.add(factory.createStatement(sentenceIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(entity.getId())));
        if (entity.getSentimentData() != null && entity.getSentimentData().getSentiment() != null) {
            addSentenceSentimentIntoStatements(statements, entity, sentenceIRI);
        }
        if (entity.getFeatureData() != null && entity.getFeatureData().getFeature() != null) {
            addSenteceFeatureIntoStatements(statements, entity, sentenceIRI);
        }
        commitChanges(statements);
        return sentenceIRI;
    }

    @Override
    public SentenceDTO update(SentenceDTO entity) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public SentenceDTO getSentenceDTO(TupleQueryResult result) {
        return null;
    }


    private void addSentenceSentimentIntoStatements(final List<Statement> statements,
                                                    final SentenceDTO sentenceDTO,
                                                    final IRI sentenceIRI) {
        String reviewId = "";
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

    private void addSenteceFeatureIntoStatements(final List<Statement> statements,
                                                 final SentenceDTO sentenceDTO,
                                                 final IRI sentenceIRI) {
        String reviewId = "";
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
    private void addSentimentIntoStatements(final List<Statement> statements,
                                            final SentenceDTO sentenceDTO,
                                            final IRI sentenceIRI) {
        IRI sentimentIRI = factory.createIRI(schemaIRI.getReactActionIRI() + "/" + sentenceDTO.getSentimentData().getSentiment());
        statements.add(factory.createStatement(sentimentIRI, schemaIRI.getTypeIRI(), schemaIRI.getReactActionIRI()));
        statements.add(factory.createStatement(sentenceIRI, schemaIRI.getPotentialActionIRI(), sentimentIRI));
        statements.add(factory.createStatement(sentimentIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(sentenceDTO.getSentimentData().getSentiment())));
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

    private void addFeatureLanguageModelIntoStatements(final SentimentDTO sentimentDTO) {

    }

    private void addSentimentLanguageModelIntoStatements(final FeatureDTO featureDTO) {

    }


    private void commitChanges(final List<Statement> statements) {
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }
}
