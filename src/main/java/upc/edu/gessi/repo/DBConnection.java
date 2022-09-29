package upc.edu.gessi.repo;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModelFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.domain.Review;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class DBConnection {
    private final Repository repository;

    private HashMap<String,String> user_map = new HashMap<>();
    private final ValueFactory factory = SimpleValueFactory.getInstance();

    //Data types
    IRI appIRI = factory.createIRI("https://schema.org/MobileApplication");
    IRI reviewIRI = factory.createIRI("https://schema.org/Review");
    IRI personIRI = factory.createIRI("https://schema.org/Person");
    IRI ratingIRI = factory.createIRI("https://schema.org/Rating");
    IRI definedTermIRI = factory.createIRI("https://schema.org/DefinedTerm");

    //App objects
    IRI identifierIRI = factory.createIRI("https://schema.org/identifier");
    IRI descriptionIRI = factory.createIRI("https://schema.org/description");
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

    //Rating objects
    IRI valueIRI = factory.createIRI("https://schema.org/ratingValue");

    IRI developerIRI = factory.createIRI("https://schema.org/Organization");

    public DBConnection(String rd4jEndpoint) {
        repository = new HTTPRepository(rd4jEndpoint);
    }

    public void insertData(String subject, String predicate, String object) {
        IRI sub = factory.createIRI(subject);
        IRI pred = factory.createIRI(predicate);
        Literal name = factory.createLiteral(object);
        Statement statement = factory.createStatement(sub,pred,name);
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
        model.add(statement);
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(model);
        repoConnection.close();
    }

    private String extractValue(Map<String,String> map, String key) {
        return map.get(key);
    }

    public void insertApp(App app, String name) throws ClassNotFoundException {
        IRI sub = factory.createIRI("http://gessi.upc.edu/app/" + name);
        Class<?> c = Class.forName("upc.edu.gessi.repo.domain.App");
        Field[] fieldList = c.getDeclaredFields();
        Method[] methodList = c.getMethods();
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
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


    public void insertApp(App app) {
        String sanitizedName = app.getApp_name().replace(" ","_");
        //sanitizedName = sanitizedName.replace("&","and");
        sanitizedName = sanitizedName.replace("|","");
        sanitizedName = sanitizedName.replace("[","");
        sanitizedName = sanitizedName.replace("]","");
        IRI sub = factory.createIRI(appIRI + "/" + sanitizedName);

        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();

        String developerName = app.getDeveloper().replace(" ","_");
        //String developerLink = app.getDeveloper()[0].getLink();



        List<Statement> statements = new ArrayList<>();
        IRI dev = factory.createIRI(developerIRI+"/"+developerName);


        statements.add(factory.createStatement(dev,authorIRI,factory.createLiteral(developerName)));
        statements.add(factory.createStatement(sub,authorIRI,dev));


        //some fields are not populated, so we check them to avoid null pointers.
        if (app.getPackage_name() != null) statements.add(factory.createStatement(sub, identifierIRI, factory.createLiteral(app.getPackage_name())));
        if (app.getDescription() != null) statements.add(factory.createStatement(sub, descriptionIRI, factory.createLiteral(app.getDescription())));
        if (app.getSummary() != null) statements.add(factory.createStatement(sub, summaryIRI, factory.createLiteral(app.getSummary())));
        if (app.getChangelog() != null) statements.add(factory.createStatement(sub, changelogIRI, factory.createLiteral(app.getChangelog())));

        //statements.add(factory.createStatement(sub, changelogIRI, factory.createLiteral(app.getChangelog())));

        for (String feature : app.getFeatures()) {
            IRI featureIRI = factory.createIRI(definedTermIRI + "/" + feature.replaceAll(" ", ""));
            statements.add(factory.createStatement(featureIRI, nameIRI, factory.createLiteral(feature)));
            statements.add(factory.createStatement(sub, featuresIRI, featureIRI));
        }
        /*for (String tag : app.getTags()) {
            statements.add(factory.createStatement(sub, tagsIRI, factory.createLiteral(tag)));
        }*/

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

        model.addAll(statements);
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(model);
        repoConnection.close();
    }


}
