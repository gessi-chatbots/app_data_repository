package upc.edu.gessi.repo.service;

import org.apache.commons.text.WordUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModelFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.domain.Review;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GraphDBService {

    private Logger logger = LoggerFactory.getLogger(GraphDBService.class);

    @Autowired
    private NLFeatureService nlFeatureService;

    private final Repository repository;

    private HashMap<String,String> user_map = new HashMap<>();
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    //Data types
    IRI appIRI = factory.createIRI("https://schema.org/MobileApplication");
    IRI reviewIRI = factory.createIRI("https://schema.org/Review");
    IRI personIRI = factory.createIRI("https://schema.org/Person");
    IRI definedTermIRI = factory.createIRI("https://schema.org/DefinedTerm");
    IRI digitalDocumentIRI = factory.createIRI("https://schema.org/DigitalDocument");

    //App objects
    IRI identifierIRI = factory.createIRI("https://schema.org/identifier");
    IRI descriptionIRI = factory.createIRI("https://schema.org/description");
    IRI disambiguatingDescriptionIRI = factory.createIRI("https://schema.org/disambiguatingDescription");
    IRI textIRI = factory.createIRI("https://schema.org/text");
    IRI summaryIRI = factory.createIRI("https://schema.org/abstract");
    IRI featuresIRI = factory.createIRI("https://schema.org/keywords");
    IRI changelogIRI = factory.createIRI("https://schema.org/releaseNotes");
    IRI reviewsIRI = factory.createIRI("https://schema.org/review");

    //Review objects
    IRI reviewBodyIRI = factory.createIRI("https://schema.org/reviewBody");

    IRI authorIRI = factory.createIRI("https://schema.org/author");
    IRI reviewRatingIRI = factory.createIRI("https://schema.org/reviewRating");

    //Person objects
    IRI nameIRI = factory.createIRI("https://schema.org/name");
    IRI developerIRI = factory.createIRI("https://schema.org/Organization");

    public GraphDBService(@Value("${db.url}") String url) {
        repository = new HTTPRepository(url);
    }

    private Model createEmptyModel() {
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
        return model;
    }

    private void commitChanges(Model model, List<Statement> statements) {
        model.addAll(statements);
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(model);
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
        if (app.getApp_name() != null) {
            String sanitizedName = sanitizeString(app.getApp_name());
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

    private String sanitizeString(String name) {
        String sanitizedName = name.replace(" ","_");
        sanitizedName = sanitizedName.replace("|","");
        sanitizedName = sanitizedName.replace("[","");
        sanitizedName = sanitizedName.replace("]","");
        return sanitizedName;
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
            IRI featureIRI = factory.createIRI(definedTermIRI + "/" + WordUtils.capitalize(feature).replaceAll(" ", ""));
            statements.add(factory.createStatement(featureIRI, nameIRI, factory.createLiteral(feature)));
            statements.add(factory.createStatement(sub, featuresIRI, featureIRI));
        }
    }

    /**
     * Deductive Methods
     */

    public void extractFeaturesByDocument(DocumentType documentType) {
        RepositoryConnection repoConnection = repository.getConnection();
        String predicate1 = "https://schema.org/" + documentType.getName();
        String predicate2 = "https://schema.org/text";
        String query = "SELECT ?subject ?object ?text WHERE { ?subject <" + predicate1 + "> ?object . ?object <"+ predicate2 +"> ?text}";
        TupleQueryResult result = runSparqlQuery(repoConnection, query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI appIRI = (IRI) bindings.getValue("subject");
            IRI documentIRI = (IRI) bindings.getValue("object");
            String text = String.valueOf(bindings.getValue("text"));

            List<String> features = nlFeatureService.getNLFeatures(text);

            //TODO insert features as nodes
            App app = new App();
            app.setFeatures(features);
            List<Statement> statements = new ArrayList<>();
            Model model = createEmptyModel();
            try {
                addFeatures(app, documentIRI, statements);
                commitChanges(model, statements);
            } catch (Exception e) {
                logger.error("There was some problem inserting features for app " + appIRI.toString() + ". Please try again later.");
            }
        }
    }

    private TupleQueryResult runSparqlQuery(RepositoryConnection repositoryConnection, String query) {
        TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

}
