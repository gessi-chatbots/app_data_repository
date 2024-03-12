package upc.edu.gessi.repo.service;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.Executor;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModelFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import upc.edu.gessi.repo.dto.*;
import upc.edu.gessi.repo.dto.graph.*;
import upc.edu.gessi.repo.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GraphDBService {

    private Logger logger = LoggerFactory.getLogger(GraphDBService.class);

    @Autowired
    private NLFeatureService nlFeatureService;

    @Autowired
    private InductiveKnowledgeService inductiveKnowledgeService;

    @Autowired
    private AppDataScannerService appDataScannerService;

    @Value("${max-days-reviews}")
    private int MAX_DAYS_REVIEWS;

    private HTTPRepository repository;

    private HashMap<String,String> user_map = new HashMap<>();
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final String prefix = "https://schema.org/";
    
    public GraphDBService(@Value("${db.url}") String url,
                          @Value("${db.username}") String username,
                          @Value("${db.password}") String password) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
    }

    private Model createEmptyModel() {
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
        return model;
    }


    public void updateRepository(String url) {
        repository = new HTTPRepository(url);
    }




    private void addFeatures(ApplicationDTO applicationDTO, IRI sub, List<Statement> statements) {
        /*
        for (Feature feature : applicationDTO.getFeatures()) {
            String id = WordUtils.capitalize(feature.getName()).replaceAll(" ", "").replaceAll("[^a-zA-Z0-9]", "");
            IRI featureIRI = factory.createIRI(definedTermIRI + "/" + id);
            statements.add(factory.createStatement(featureIRI, nameIRI, factory.createLiteral(feature.getName())));
            statements.add(factory.createStatement(featureIRI, identifierIRI, factory.createLiteral(id)));
            statements.add(factory.createStatement(sub, featuresIRI, featureIRI));
            statements.add(factory.createStatement(featureIRI, typeIRI, definedTermIRI));
        }
         */
    }

    /**
     * Deductive Methods
     */


    private int count;

    public int getCount() {return count;}
    /*
    private int executeFeatureQuery(RepositoryConnection repoConnection, String query, int batchSize, int from) {
        TupleQueryResult result = Utils.runSparqlQuery(repoConnection, query);

        List<AnalyzedDocument> analyzedDocuments = new ArrayList<>();
        List<IRI> source = new ArrayList<>();

        count = 1;

        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if (count >= from) {
                try {

                    IRI appIRI = (IRI) bindings.getValue("subject");
                    IRI documentIRI = (IRI) bindings.getValue("object");
                    String text = bindings.getValue("text").stringValue();

                    analyzedDocuments.add(new AnalyzedDocument(documentIRI.toString(), text));

                    if (documentIRI.toString().contains(reviewIRI.toString())) {
                        String reviewSource = this.digitalDocumentIRI.toString()
                                + appIRI.toString().replace(this.appIRI.toString(), "")
                                + "-" + DocumentType.REVIEWS;
                        documentIRI = factory.createIRI(reviewSource);
                    }

                    source.add(documentIRI);

                    if (count % batchSize == 0) {
                        runFeatureExtractionBatch(analyzedDocuments, source, count, appIRI);

                        analyzedDocuments = new ArrayList<>();
                        source = new ArrayList<>();
                    }
                } catch (Exception e) {
                    return count;
                }

            }
            ++count;
        }

        // Run last batch
        if (count % batchSize != 1)
            runFeatureExtractionBatch(analyzedDocuments, source, count, appIRI);

        return -1;

    }
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
        executeFeatureQuery(repository.getConnection(), query, batchSize, 0);
    }

    public int extractFeaturesFromReviews(int batchSize, int from) {
        String query = "SELECT ?subject ?object ?text WHERE {?subject <https://schema.org/review> ?object . " +
                "?object <https://schema.org/reviewBody> ?text}";

        return executeFeatureQuery(repository.getConnection(), query, batchSize, from);
    }


    private void runFeatureExtractionBatch(List<AnalyzedDocument> analyzedDocuments, List<IRI> source, int count, IRI appIRI) {
        List<AnalyzedDocument> features = nlFeatureService.getNLFeatures(analyzedDocuments);
        Model model = createEmptyModel();
        List<Statement> statements = new ArrayList<>();

        for (int i = 0; i < features.size(); ++i) {
            ApplicationDTO applicationDTO = new ApplicationDTO();
            List<String> featureString = features.get(i).getFeatures();
            List<Feature> featureList = new ArrayList<>();
            for (String fs : featureString) {
                featureList.add(new Feature(appIRI.toString(), fs));
            }
            // applicationDTO.setFeatures(featureList);
            try {
                addFeatures(applicationDTO, source.get(i), statements);
            } catch (Exception e) {
                logger.error("There was some problem inserting features for app " + appIRI.toString() + ". Please try again later.");
            }
        }
        commitChanges(model, statements);
        logger.info(count + " documents already processed. Keep going...");
    }
*/

    public List<GraphDocument> getDocumentsByApp(String app) {
        List<GraphDocument> apps = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select * where {\n" +
                "    <" + app + "> (schema:description | schema:abstract | schema:releaseNotes) ?document .\n" +
                "    ?document schema:text ?text ;\n" +
                "              schema:disambiguatingDescription ?disDescription \n" +
                "} ";
        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI document = (IRI) bindings.getValue("document");
            String text = bindings.getValue("text").stringValue();
            String disDescription = bindings.getValue("disDescription").stringValue();


            GraphDocument graphDocument = new GraphDocument(document.toString(), text, disDescription);
            apps.add(graphDocument);
        }

        return apps;
    }

    private List<GraphFeature> getFeatures(String nodeId) {
        List<GraphFeature> features = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select ?feature ?name where {\n" +
                "    <"+ nodeId +"> schema:feature ?keywords .\n" +
                "    ?feature schema:name ?name\n" +
                "} ";

        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI feature = (IRI) bindings.getValue("feature");
            String name = bindings.getValue("name").stringValue();

            GraphFeature graphFeature = new GraphFeature(feature.toString(), name);
            features.add(graphFeature);
        }

        return features;

    }

    private List<GraphReview> getReviews(String nodeId) {
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

    /*
    public void getAppsWithFeatures() {

        List<GraphApp> apps = getAllApps();

        int count = 1;

        for (GraphApp app : apps) {
            List<GraphNode> nodes = new ArrayList<>();
            List<GraphEdge> edges = new ArrayList<>();

            nodes.add(app);

            logger.info("Transforming app #" + count + ": " + app.getIdentifier());
            ++count;
            //Add documents
            List<GraphDocument> graphDocuments = getDocumentsByApp(app.getNodeId());
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
            List<GraphReview> graphReviews = getReviews(app.getNodeId());
            nodes.addAll(graphReviews);
            for (GraphReview graphReview : graphReviews) {
                edges.add(new GraphEdge(app.getNodeId(), graphReview.getNodeId()));

                List<GraphFeature> documentFeatures = getFeatures(graphReview.getNodeId());
                nodes.addAll(documentFeatures);
                for (GraphFeature feature : documentFeatures) {
                    edges.add(new GraphEdge(graphReview.getNodeId(), feature.getNodeId()));
                }
            }
            inductiveKnowledgeService.addNodes(nodes);
            inductiveKnowledgeService.addEdges(edges);
        }
        //return new Graph(nodes, edges);
    }
*/
    public List<IRI> getAllFeatures() {
        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "SELECT distinct ?documentText WHERE {\n" +
                "    ?app (schema:description | schema:abstract | schema:releaseNotes | schema:featureList) ?documentID .\n" +
                "    ?documentID schema:keywords ?documentText\n" +
                "}";
        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);
        List<IRI> features = new ArrayList<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            features.add((IRI) bindings.getValue("documentText"));
        }
        return features;
    }

    /*
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
        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        List<IRI> connectedFeatures = new ArrayList<>();

        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if (Double.parseDouble(bindings.getValue("score").stringValue()) >= synonymThreshold)
                connectedFeatures.add((IRI) bindings.getValue("documentID"));
        }

        List<Statement> statements = new ArrayList<>();
        //TODO use term-by-term query to check if terms are also synonyms
        for (int i = 1; i < connectedFeatures.size(); ++i) {
            statements.add(factory.createStatement(feature, synonymIRI, connectedFeatures.get(i)));
        }
        commitChanges(createEmptyModel(), statements);
    }*/

    public List<SimilarityApp> getTopKSimilarApps(String app, int k, DocumentType documentType) {
        String query = "PREFIX :<http://www.ontotext.com/graphdb/similarity/>\n" +
                "PREFIX inst:<http://www.ontotext.com/graphdb/similarity/instance/>\n" +
                "PREFIX pubo: <http://ontology.ontotext.com/publishing#>\n" +
                "\n" +
                "SELECT ?documentID (group_concat(distinct ?category;separator=\";\") as ?categories) ?score {\n" +
                "    ?search a inst:apps_by_" + documentType.getName() + " ;\n" +
                "        :searchDocumentID <https://schema.org/MobileApplication/" + app + ">;\n" +
                "        :searchParameters \"-numsearchresults " + k +" \";\n" +
                "        :documentResult ?result .\n" +
                "    ?result :value ?documentID ;\n" +
                "            :score ?score.\n" +
                "    ?documentID <https://schema.org/applicationCategory> ?category\n" +
                "} GROUP BY ?documentID ?score";
        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        List<SimilarityApp> similarApps = new ArrayList<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            similarApps.add(new SimilarityApp(bindings.getValue("categories").stringValue().split(";"),
                    bindings.getValue("documentID").stringValue(),
                    Double.parseDouble(bindings.getValue("score").stringValue())));
        }
        return similarApps;
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
        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        List<SimilarityApp> similarApps = new ArrayList<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            similarApps.add(new SimilarityApp(bindings.getValue("categories").stringValue().split(";"),
                    bindings.getValue("documentID").stringValue(),
                    Double.parseDouble(bindings.getValue("score").stringValue())));
        }
        return similarApps;
    }

    public void deleteSameAsRelations() {
        String query = "delete where { \n" +
                "    ?x <https://schema.org/sameAs> ?z .\n" +
                "}";
        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        int counter = 0;
        while (result.next() != null) ++counter;

        logger.info(counter + " similarity relations deleted");
    }

    public void exportRepository(String fileName) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        FileOutputStream outputStream = new FileOutputStream("src/main/resources/exports/" + fileName);
        RDFWriter writer = Rio.createWriter(RDFFormat.RDFJSON, outputStream);
        connection.exportStatements(null, null, null, false, writer);
        IOUtils.closeQuietly(outputStream);
    }



    public void insertRDF(MultipartFile file) throws Exception {
        // Parse the Turtle file into an RDF model
        try (InputStream inputStream = file.getInputStream()) {
            Model model = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            RepositoryConnection repoConnection = repository.getConnection();
            repoConnection.begin();
            repoConnection.add(model);
            repoConnection.commit();
            repoConnection.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public void insertRML(String jsonFolder, File mappingFile) throws Exception {
        InputStream mappingStream = new FileInputStream(mappingFile);
        QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

        // Create RecordsGenerator for JSON data
        RecordsFactory recordsFactory = new RecordsFactory(jsonFolder);

        QuadStore outputStore = new RDF4JStore();
        // Set up the functions used during the mapping
        Agent functionAgent = AgentFactory.createFromFnO(
                "fno/functions_idlab.ttl",
                "fno/functions_idlab_classes_java_mapping.ttl",
                "grel_java_mapping.ttl",
                "functions_grel.ttl");

        // Create the Executor
        Executor executor = new Executor(rmlStore, recordsFactory, outputStore, be.ugent.rml.Utils.getBaseDirectiveTurtle(mappingStream), functionAgent);
        // Execute the mapping
        QuadStore result = executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        // Optionally, you can convert the result to an RDF4J Model
        System.out.println(result);

    }

    /*private static Model convertQuadStoreToRDF4JModel(QuadStore quadStore) {
        Model rdf4jModel = new LinkedHashModel();

        quadStore.ite(quad -> {
            rdf4jModel.add(
                    SimpleValueFactory.getInstance().createIRI(quad.getSubject().getValue()),
                    SimpleValueFactory.getInstance().createIRI(quad.getPredicate().getValue()),
                    SimpleValueFactory.getInstance().createLiteral(quad.getObject().getValue()),
                    SimpleValueFactory.getInstance().createIRI(quad.getGraph().getValue())
            );
        });

        return rdf4jModel;
    }*/
}
