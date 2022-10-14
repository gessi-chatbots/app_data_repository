package upc.edu.gessi.repo.service;

import org.apache.commons.text.WordUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModelFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.domain.*;
import upc.edu.gessi.repo.domain.graph.*;
import upc.edu.gessi.repo.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GraphDBService {

    private Logger logger = LoggerFactory.getLogger(GraphDBService.class);

    @Autowired
    private NLFeatureService nlFeatureService;

    @Autowired
    private InductiveKnowledgeService inductiveKnowledgeService;

    private final Repository repository;

    private HashMap<String,String> user_map = new HashMap<>();
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final String prefix = "https://schema.org/";

    //Data types
    private IRI appIRI = factory.createIRI("https://schema.org/MobileApplication");
    private IRI reviewIRI = factory.createIRI("https://schema.org/Review");
    private IRI personIRI = factory.createIRI("https://schema.org/Person");
    private IRI definedTermIRI = factory.createIRI("https://schema.org/DefinedTerm");
    private IRI digitalDocumentIRI = factory.createIRI("https://schema.org/DigitalDocument");

    //App objects
    private IRI identifierIRI = factory.createIRI("https://schema.org/identifier");
    private IRI categoryIRI = factory.createIRI("https://schema.org/applicationCategory");
    private IRI descriptionIRI = factory.createIRI("https://schema.org/description");
    private  IRI disambiguatingDescriptionIRI = factory.createIRI("https://schema.org/disambiguatingDescription");
    private IRI textIRI = factory.createIRI("https://schema.org/text");
    private IRI summaryIRI = factory.createIRI("https://schema.org/abstract");
    private  IRI featuresIRI = factory.createIRI("https://schema.org/keywords");
    private  IRI changelogIRI = factory.createIRI("https://schema.org/releaseNotes");
    private  IRI reviewsIRI = factory.createIRI("https://schema.org/review");
    private IRI reviewDocumentIRI = factory.createIRI("https://schema.org/featureList");

    //Review objects
    private  IRI reviewBodyIRI = factory.createIRI("https://schema.org/reviewBody");

    private  IRI authorIRI = factory.createIRI("https://schema.org/author");
    private  IRI reviewRatingIRI = factory.createIRI("https://schema.org/reviewRating");

    //Person objects
    private  IRI nameIRI = factory.createIRI("https://schema.org/name");
    private  IRI developerIRI = factory.createIRI("https://schema.org/Organization");

    //Feature object
    private IRI synonymIRI = factory.createIRI("https://schema.org/sameAs");

    public GraphDBService(@Value("${db.url}") String url) {
        repository = new HTTPRepository(url);
    }

    private Model createEmptyModel() {
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
        return model;
    }

    private void commitChanges(Model model, List<Statement> statements) {
        //model.addAll(statements);
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }

    //TODO delete dead code
    /*public void insertData(String subject, String predicate, String object) {
        IRI sub = factory.createIRI(subject);
        IRI pred = factory.createIRI(predicate);
        Literal name = factory.createLiteral(object);
        Statement statement = factory.createStatement(sub,pred,name);
        Model model = createEmptyModel();
        model.add(statement);
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(model);
        repoConnection.close();
    }

    private String extractValue(Map<String,String> map, String key) {
        return map.get(key);
    }*/

    @Deprecated
    public void insertApp(App app, String name) throws ClassNotFoundException {
        IRI sub = factory.createIRI("http://gessi.upc.edu/app/" + name);
        Class<?> c = Class.forName("upc.edu.gessi.repo.domain.App");
        Field[] fieldList = c.getDeclaredFields();
        Method[] methodList = c.getMethods();
        Model model = createEmptyModel();
        for (Field f : fieldList) {
            IRI pred = factory.createIRI("https://schema.org/" + f.getName());
            Object obj = "";
            for (Method m : methodList) {
                if (m.getName().length() == (f.getName().length() + 3) && m.getName().toLowerCase().equals("get"+f.getName().toLowerCase())) {
                    try {
                        obj = m.invoke(app);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (obj instanceof List<?>) {
                for (Object s : Collections.unmodifiableList((List) obj)) {
                    Review review = (Review) s;
                    Literal object = factory.createLiteral(review.getReview());
                    Statement statement = factory.createStatement(sub, pred, object);
                    model.add(statement);
                }
            }
            else {
                Literal object = factory.createLiteral(obj == null ? "Null" : (String) obj);
                Statement statement = factory.createStatement(sub, pred, object);
                model.add(statement);
            }

        }
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(model);
        repoConnection.close();
    }

    /**
     * Insert Apps
     * @param app
     */
    public void insertApp(App app) {

        List<Statement> statements = new ArrayList<>();
        Model model = createEmptyModel();

        IRI sub = factory.createIRI(appIRI + "/" + app.getPackage_name());

        String developerName = app.getDeveloper().replace(" ","_");
        IRI dev = factory.createIRI(developerIRI+"/"+developerName);

        statements.add(factory.createStatement(dev,authorIRI,factory.createLiteral(developerName)));
        statements.add(factory.createStatement(sub,authorIRI,dev));

        //some fields are not populated, so we check them to avoid null pointers.
        for (AppCategory category : app.getCategories()) {
            statements.add(factory.createStatement(sub, categoryIRI, factory.createLiteral(String.valueOf(category))));
        }
        if (app.getApp_name() != null) {
            String sanitizedName = Utils.sanitizeString(app.getApp_name());
            statements.add(factory.createStatement(sub, nameIRI, factory.createLiteral(sanitizedName)));
        }
        if (app.getPackage_name() != null) statements.add(factory.createStatement(sub, identifierIRI, factory.createLiteral(app.getPackage_name())));

        if (app.getDescription() != null) {
            addDigitalDocument(app.getPackage_name(), app.getDescription(), statements, sub, descriptionIRI, DocumentType.DESCRIPTION);
            //statements.add(factory.createStatement(sub, descriptionIRI, factory.createLiteral(app.getDescription())));
        }
        if (app.getSummary() != null) {
            addDigitalDocument(app.getPackage_name(), app.getSummary(), statements, sub, summaryIRI, DocumentType.SUMMARY);
            //statements.add(factory.createStatement(sub, summaryIRI, factory.createLiteral(app.getSummary())));
        }
        if (app.getChangelog() != null) {
            addDigitalDocument(app.getPackage_name(), app.getChangelog(), statements, sub, changelogIRI, DocumentType.CHANGELOG);
            //statements.add(factory.createStatement(sub, changelogIRI, factory.createLiteral(app.getChangelog())));
        }
        //Adding reviewDocumentPlaceholder
        addDigitalDocument(app.getPackage_name(), "Aggregated NL data for app " + app.getApp_name(), statements, sub, reviewDocumentIRI, DocumentType.REVIEWS);

        //Adding all features - given features are injected at creation time, they are user annotated
        addFeatures(app, sub, statements);
        //Adding all reviews
        addReviews(app, sub, statements);
        //Committing all changes
        commitChanges(model, statements);
    }

    private void addDigitalDocument(String packageName, String text, List<Statement> statements, IRI sub, IRI pred, DocumentType documentType) {
        IRI appDescription = factory.createIRI(digitalDocumentIRI + "/" + packageName + "-" + documentType);
        statements.add(factory.createStatement(appDescription, textIRI, factory.createLiteral(text)));
        statements.add(factory.createStatement(appDescription, disambiguatingDescriptionIRI, factory.createLiteral(documentType.getName())));
        statements.add(factory.createStatement(sub, pred, appDescription));
    }

    private void addReviews(App app, IRI sub, List<Statement> statements) {
        for (Review r : app.getReviews()) {
             IRI review = factory.createIRI(reviewIRI + "/" + r.getReviewId());
             //normalize the text to utf-8 encoding
             String reviewBody = r.getReview();
             if (reviewBody != null) {
                 byte[] reviewBytes = reviewBody.getBytes();
                 String encoded_string = new String(reviewBytes, StandardCharsets.UTF_8);
                 statements.add(factory.createStatement(review, reviewBodyIRI, factory.createLiteral(encoded_string)));
             }

             //normalize the text to utf-8 encoding
             String reviewAuthor = r.getUserName();
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
             statements.add(factory.createStatement(review, authorIRI, author));
             //IRI rating = factory.createIRI(reviewRatingIRI)
             statements.add(factory.createStatement(review, reviewRatingIRI, factory.createLiteral(r.getScore())));
             statements.add(factory.createStatement(sub, reviewsIRI, review));
         }
    }

    private void addFeatures(App app, IRI sub, List<Statement> statements) {
        for (String feature : app.getFeatures()) {
            IRI featureIRI = factory.createIRI(definedTermIRI + "/" + WordUtils.capitalize(feature).replaceAll(" ", "").replaceAll("[^a-zA-Z0-9]", ""));
            statements.add(factory.createStatement(featureIRI, nameIRI, factory.createLiteral(feature)));
            statements.add(factory.createStatement(sub, featuresIRI, featureIRI));
        }
    }

    /**
     * Deductive Methods
     */

    public void extractFeaturesByDocument(DocumentType documentType) {
        String predicateQueue = null;
        switch(documentType) {
            case SUMMARY -> predicateQueue = "abstract";
            case CHANGELOG -> predicateQueue = "releaseNotes";
            default -> predicateQueue = "description";
        }
        String predicate1 = "https://schema.org/" + predicateQueue;
        String predicate2 = "https://schema.org/text";
        String query = "SELECT ?subject ?object ?text WHERE { ?subject <" + predicate1 + "> ?object . ?object <"+ predicate2 +"> ?text}";
        executeFeatureQuery(repository.getConnection(), query);
    }

    public void extractFeaturesFromReviews() {
        String query = "SELECT ?subject ?object ?text WHERE {?subject <https://schema.org/review> ?object . " +
                "?object <https://schema.org/reviewBody> ?text}";

        executeFeatureQuery(repository.getConnection(), query);
    }

    private final int BATCH_SIZE = 2;

    private void executeFeatureQuery(RepositoryConnection repoConnection, String query) {
        TupleQueryResult result = Utils.runSparqlQuery(repoConnection, query);

        List<AnalyzedDocument> analyzedDocuments = new ArrayList<>();
        List<IRI> source = new ArrayList<>();

        int count = 1;

        while (result.hasNext()) {
            BindingSet bindings = result.next();

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

            if (count % BATCH_SIZE == 0) {
                runFeatureExtractionBatch(analyzedDocuments, source, count, appIRI);

                analyzedDocuments = new ArrayList<>();
                source = new ArrayList<>();
            }

            ++count;
        }

        // Run last batch
        if (count % BATCH_SIZE != 1)
            runFeatureExtractionBatch(analyzedDocuments, source, count, appIRI);

    }

    private void runFeatureExtractionBatch(List<AnalyzedDocument> analyzedDocuments, List<IRI> source, int count, IRI appIRI) {
        List<AnalyzedDocument> features = nlFeatureService.getNLFeatures(analyzedDocuments);
        Model model = createEmptyModel();
        List<Statement> statements = new ArrayList<>();

        for (int i = 0; i < features.size(); ++i) {
            App app = new App();
            app.setFeatures(features.get(i).getFeatures());
            try {
                addFeatures(app, source.get(i), statements);
            } catch (Exception e) {
                logger.error("There was some problem inserting features for app " + appIRI.toString() + ". Please try again later.");
            }
        }
        commitChanges(model, statements);
        logger.info(count + " documents already processed. Keep going...");
    }

    public List<GraphApp> getAllApps() {
        List<GraphApp> apps = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select ?app ?identifier ?name (GROUP_CONCAT(?applicationCategory;separator=\",\") As ?categories) where {\n" +
                "    ?app schema:identifier ?identifier ;\n" +
                "         schema:name ?name ;\n" +
                "         schema:applicationCategory ?applicationCategory\n" +
                "} group by ?app ?identifier ?name";
        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI app = (IRI) bindings.getValue("app");
            String identifier = bindings.getValue("identifier").stringValue();
            String name = bindings.getValue("name").stringValue();
            String[] categories = bindings.getValue("categories").stringValue().split(",");

            GraphApp graphApp = new GraphApp(app.toString(), identifier, name, categories);
            apps.add(graphApp);
        }

        return apps;
    }

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

    private double synonymThreshold = 0.5;

    public void connectFeatureWithSynonyms(IRI feature) {
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
    }

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
}
