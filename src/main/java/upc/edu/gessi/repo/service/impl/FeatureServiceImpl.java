package upc.edu.gessi.repo.service.impl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.*;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.graph.*;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.RepositoryFactory;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.service.FeatureService;
import upc.edu.gessi.repo.util.FeatureQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.util.ArrayList;
import java.util.List;

@Service
@Lazy
public class FeatureServiceImpl implements FeatureService {

    private Logger logger = LoggerFactory.getLogger(FeatureServiceImpl.class);

    private final HTTPRepository repository;
    private final SchemaIRI schemaIRI;
    private final InductiveKnowledgeServiceImpl inductiveKnowledgeServiceImpl;
    private final ReviewServiceImpl reviewServiceImpl;

    private final MobileApplicationServiceImpl applicationService;
    private final NLFeatureServiceImpl nlFeatureServiceImpl;
    private final ValueFactory factory = SimpleValueFactory.getInstance();
    private final DocumentServiceImpl documentServiceImpl;

    private final FeatureQueryBuilder featureQueryBuilder;

    private final RepositoryFactory repositoryFactory;


    @Autowired
    public FeatureServiceImpl(
            final @Value("${db.url}") String url,
            final @Value("${db.username}") String username,
            final @Value("${db.password}") String password,
            final SchemaIRI schema,
            final NLFeatureServiceImpl nlFeatureServiceImpl,
            final MobileApplicationServiceImpl applicationSv,
            final DocumentServiceImpl documentSv,
            final InductiveKnowledgeServiceImpl iks,
            final ReviewServiceImpl revSv,
            final FeatureQueryBuilder ftQueryBuilder,
            final RepositoryFactory repoFact) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        schemaIRI = schema;
        this.nlFeatureServiceImpl = nlFeatureServiceImpl;
        applicationService = applicationSv;
        documentServiceImpl = documentSv;
        inductiveKnowledgeServiceImpl = iks;
        reviewServiceImpl = revSv;
        featureQueryBuilder = ftQueryBuilder;
        repositoryFactory = repoFact;
    }

    @Override
    public void extractFeaturesByDocument(DocumentType documentType, int batchSize) {
        String predicateQueue = null;
        switch(documentType) {
            case SUMMARY -> predicateQueue = "abstract";
            case CHANGELOG -> predicateQueue = "releaseNotes";
            default -> predicateQueue = "description";
        }
        String predicate1 = "https://schema.org/" + predicateQueue;
        String predicate2 = "https://schema.org/text";
        String query = "SELECT ?subject ?object ?text WHERE { ?subject <" + predicate1 + "> ?object . ?object <"+ predicate2 +"> ?text}";
        executeFeatureQuery(repository.getConnection(), query, false, batchSize, 0);
    }

    @Override
    public Integer extractFeaturesFromReviews(final Integer batchSize,
                                              final Integer from,
                                              final String featureModel) {
        List<ReviewDTO> reviewDTOList = new ArrayList<>();
        if (batchSize == 0 && from == 0) {
            int reviewCount = reviewServiceImpl.getReviewCount();
            logger.info("Obtained a total of {} reviews for analysis", reviewCount);
            int batchSizeAux = 1000;
            int fromAux = 0;
            int totalReviewsProcessed = 0;
            try {
                while (totalReviewsProcessed <= reviewCount) {
                    List<ReviewDTO> reviewAuxList = reviewServiceImpl.getBatched(batchSizeAux, fromAux);
                    logger.info("Obtained a total of {} batched reviews for analysis", reviewAuxList.size());
                    if (reviewAuxList.isEmpty()) {
                        break;
                    } else {
                        reviewDTOList.addAll(reviewAuxList);
                    }
                    fromAux += batchSizeAux;
                    totalReviewsProcessed += reviewAuxList.size();
                    logger.info("Retrieved {} reviews, total reviews processed: {}", reviewAuxList.size(), totalReviewsProcessed);
                }

            } catch (Exception e) {
                Utils.serializeReviews(reviewDTOList, logger);
            }
        } else {
            reviewDTOList = reviewServiceImpl.getBatched(batchSize, from);
            logger.info("Retrieved {} reviews starting from offset {}", reviewDTOList.size(), from);

        }
        List<ReviewDTO> analyzedReviewsDTOList = nlFeatureServiceImpl.getHUBFeatures(reviewDTOList, featureModel);
        logger.info("Obtained {} analyzed reviews from HUB ", analyzedReviewsDTOList.size());
        reviewServiceImpl.create(analyzedReviewsDTOList);
        logger.info("Processed and created {} analyzed reviews", analyzedReviewsDTOList.size());

        return 0;
    }





    @Override
    public List<Feature> create(List<Feature> dtos) {
        return null;
    }

    @Override
    public Feature get(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<Feature> getListed(List<String> ids) throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<Feature> getAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<Feature> getAll() {
        return null;
    }

    @Override
    public void update(Feature entity) {

    }

    @Override
    public void delete(String id) {
    }
    public List<IRI> getAllFeatures() {
        String query = featureQueryBuilder.findAllDocumentFeaturesQuery();
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);
        List<IRI> features = new ArrayList<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            features.add((IRI) bindings.getValue("documentText"));
        }
        return features;
    }
    public void connectFeatureWithSynonyms(IRI feature, double synonymThreshold) {
        String query = "PREFIX :<http://www.ontotext.com/graphdb/similarity/>\n" +
                "PREFIX inst:<http://www.ontotext.com/graphdb/similarity/instance/>\n" +
                "PREFIX pubo: <http://ontology.ontotext.com/publishing#>\n" +
                "\n" +
                "SELECT distinct ?documentID ?score {\n" +
                "    ?search a inst:feature_index ;\n" +
                "        :searchDocumentID \n" +
                "        <"+ feature.toString() + ">;\n" +
                "        :searchParameters \"\";\n" +
                "        :documentResult ?result .\n" +
                "    ?result :value ?documentID ;\n" +
                "            :score ?score.\n" +
                "}\n";
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

        List<IRI> connectedFeatures = new ArrayList<>();

        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if (Double.parseDouble(bindings.getValue("score").stringValue()) >= synonymThreshold)
                connectedFeatures.add((IRI) bindings.getValue("documentID"));
        }

        List<Statement> statements = new ArrayList<>();
        //TODO use term-by-term query to check if terms are also synonyms
        for (int i = 1; i < connectedFeatures.size(); ++i) {
            statements.add(factory.createStatement(feature, schemaIRI.getSynonymIRI(), connectedFeatures.get(i)));
        }
        commitChanges(statements);
    }

    public List<SimilarityApp> getTopKAppsByFeature(String feature, Integer k, DocumentType documentType) {
        String query = "PREFIX :<http://www.ontotext.com/graphdb/similarity/>\n" +
                "PREFIX inst:<http://www.ontotext.com/graphdb/similarity/instance/>\n" +
                "PREFIX pubo: <http://ontology.ontotext.com/publishing#>\n" +
                "\n" +
                "SELECT ?documentID (group_concat(distinct ?category;separator=\";\") as ?categories) ?score {\n" +
                "    ?search a inst:apps_by_"+ documentType.getName()+" ;\n" +
                "        :searchTerm \""+ feature +"\";\n" +
                "        :searchParameters \"\";\n" +
                "        :searchParameters \"-numsearchresults " + k +" \";\n" +
                "        :documentResult ?result .\n" +
                "    ?result :value ?documentID ;\n" +
                "            :score ?score.\n" +
                "    ?documentID <https://schema.org/applicationCategory> ?category\n" +
                "} GROUP BY ?documentID ?score";
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

        List<SimilarityApp> similarApps = new ArrayList<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            similarApps.add(new SimilarityApp(bindings.getValue("categories").stringValue().split(";"),
                    bindings.getValue("documentID").stringValue(),
                    Double.parseDouble(bindings.getValue("score").stringValue())));
        }
        return similarApps;
    }
    public void getAppsWithFeatures() {

        List<GraphApp> apps = applicationService.getAllApps();

        int count = 1;

        for (GraphApp app : apps) {
            List<GraphNode> nodes = new ArrayList<>();
            List<GraphEdge> edges = new ArrayList<>();

            nodes.add(app);
            ++count;

            //Add documents
            List<GraphDocument> graphDocuments = documentServiceImpl.getDocumentsByApp(app.getNodeId());
            nodes.addAll(graphDocuments);
            for (GraphDocument document : graphDocuments) {
                edges.add(new GraphEdge(app.getNodeId(), document.getNodeId()));

                List<GraphFeature> documentFeatures = getFeatures(document.getNodeId());
                nodes.addAll(documentFeatures);
                for (GraphFeature feature : documentFeatures) {
                    edges.add(new GraphEdge(document.getNodeId(), feature.getNodeId()));
                }
            }

            //Add annotated features
            List<GraphFeature> annotatedFeatures = getFeatures(app.getNodeId());
            nodes.addAll(annotatedFeatures);
            for (GraphFeature graphAnnotatedFeature : annotatedFeatures) {
                edges.add(new GraphEdge(app.getNodeId(), graphAnnotatedFeature.getNodeId()));
            }

            //Add reviews
            List<GraphReview> graphReviews = ((ReviewRepository) useRepository(ReviewRepository.class)).getReviews(app.getNodeId());
            nodes.addAll(graphReviews);
            for (GraphReview graphReview : graphReviews) {
                edges.add(new GraphEdge(app.getNodeId(), graphReview.getNodeId()));

                List<GraphFeature> documentFeatures = getFeatures(graphReview.getNodeId());
                nodes.addAll(documentFeatures);
                for (GraphFeature feature : documentFeatures) {
                    edges.add(new GraphEdge(graphReview.getNodeId(), feature.getNodeId()));
                }
            }
            inductiveKnowledgeServiceImpl.addNodes(nodes);
            inductiveKnowledgeServiceImpl.addEdges(edges);
        }
        //return new Graph(nodes, edges);
    }

    private void runFeatureExtractionBatch(List<AnalyzedDocumentDTO> analyzedDocumentDTOS, List<IRI> source, int count, IRI appIRI) {
        List<AnalyzedDocumentDTO> analyzedDocuments = nlFeatureServiceImpl.getNLFeatures(analyzedDocumentDTOS);
        List<Statement> statements = new ArrayList<>();

        for (int i = 0; i < analyzedDocuments.size(); ++i) {
            MobileApplicationFullDataDTO completeApplicationDataDTO = new MobileApplicationFullDataDTO();
            List<String> featureString = analyzedDocuments.get(i).getFeatures();
            List<Feature> featureList = new ArrayList<>();
            for (String fs : featureString) {
                featureList.add(new Feature(appIRI.toString(), fs));
            }
            completeApplicationDataDTO.setFeatures(
                    featureList
                            .stream()
                            .map(Feature::getName)
                            .toList());
            try {
                applicationService
                        .addFeatures(
                                completeApplicationDataDTO,
                                source.get(i),
                                statements);
            } catch (Exception e) {
                logger.error("There was some problem inserting analyzedDocuments for app " + appIRI.toString() + ". Please try again later.");
            }
        }
        commitChanges(statements);
        logger.info(count + " documents already processed. Keep going...");
    }
    private List<GraphFeature> getFeatures(String nodeId) {
        List<GraphFeature> features = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select ?feature ?name where {\n" +
                "    <"+ nodeId +"> schema:feature ?keywords .\n" +
                "    ?feature schema:name ?name\n" +
                "} ";

        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI feature = (IRI) bindings.getValue("feature");
            String name = bindings.getValue("name").stringValue();

            GraphFeature graphFeature = new GraphFeature(feature.toString(), name);
            features.add(graphFeature);
        }

        return features;
    }
    private void commitChanges(final List<Statement> statements) {
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }

    private int executeReviewFeatureQuery() {
       return 0;
    }

    private int executeFeatureQuery(RepositoryConnection repoConnection, String query,  boolean batched, Integer batchSize, Integer from) {
        Integer count;
        TupleQueryResult result = Utils.runSparqlSelectQuery(repoConnection, query);

        List<AnalyzedDocumentDTO> analyzedDocumentDTOS = new ArrayList<>();
        List<IRI> source = new ArrayList<>();

        count = 1;

        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if (count >= from) {
                try {

                    IRI appIRI = (IRI) bindings.getValue("subject");
                    IRI documentIRI = (IRI) bindings.getValue("object");
                    String text = bindings.getValue("text").stringValue();

                    analyzedDocumentDTOS.add(new AnalyzedDocumentDTO(documentIRI.toString(), text));

                    if (documentIRI.toString().contains(schemaIRI.getReviewIRI().toString())) {
                        String reviewSource = schemaIRI.getDigitalDocumentIRI().toString()
                                + appIRI.toString().replace(schemaIRI.getAppIRI().toString(), "")
                                + "-" + DocumentType.REVIEWS;
                        documentIRI = factory.createIRI(reviewSource);
                    }

                    source.add(documentIRI);

                    if (batched && (count % batchSize == 0)) {
                        runFeatureExtractionBatch(analyzedDocumentDTOS, source, count, appIRI);

                        analyzedDocumentDTOS = new ArrayList<>();
                        source = new ArrayList<>();
                    } else {
                        runFeatureExtractionBatch(analyzedDocumentDTOS, source, count, appIRI);
                    }
                } catch (Exception e) {
                    return count;
                }

            }
            ++count;
        }

        // Run last batch
        if (batched && (count % batchSize != 1)) {
            runFeatureExtractionBatch(analyzedDocumentDTOS, source, count, schemaIRI.getAppIRI());
        }

        return -1;

    }


    private Object useRepository(Class<?> clazz) {
        return repositoryFactory.createRepository(clazz);
    }

}
