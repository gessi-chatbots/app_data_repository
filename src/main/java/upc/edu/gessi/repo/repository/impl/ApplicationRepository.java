package upc.edu.gessi.repo.repository.impl;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModelFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.ApplicationDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.ReviewDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.RdfRepository;
import upc.edu.gessi.repo.service.AppDataScannerService;
import upc.edu.gessi.repo.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ApplicationRepository <T> implements RdfRepository {

    private final HTTPRepository repository;

    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final AppDataScannerService appDataScannerService;

    // Prefix
    private final String prefix = "https://schema.org/";
    // Data types
    private IRI typeIRI = factory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private IRI appIRI = factory.createIRI("https://schema.org/MobileApplication");
    private IRI reviewIRI = factory.createIRI("https://schema.org/Review");
    private IRI personIRI = factory.createIRI("https://schema.org/Person");
    private IRI definedTermIRI = factory.createIRI("https://schema.org/DefinedTerm");
    private IRI digitalDocumentIRI = factory.createIRI("https://schema.org/DigitalDocument");
    private  IRI developerIRI = factory.createIRI("https://schema.org/Organization");

    //App objects
    private IRI identifierIRI = factory.createIRI("https://schema.org/identifier");
    private IRI categoryIRI = factory.createIRI("https://schema.org/applicationCategory");
    private IRI descriptionIRI = factory.createIRI("https://schema.org/description");
    private IRI disambiguatingDescriptionIRI = factory.createIRI("https://schema.org/disambiguatingDescription");
    private IRI textIRI = factory.createIRI("https://schema.org/text");
    private IRI summaryIRI = factory.createIRI("https://schema.org/abstract");
    private IRI featuresIRI = factory.createIRI("https://schema.org/keywords");
    private IRI changelogIRI = factory.createIRI("https://schema.org/releaseNotes");
    private IRI reviewsIRI = factory.createIRI("https://schema.org/review");
    private IRI reviewDocumentIRI = factory.createIRI("https://schema.org/featureList");
    private IRI sameAsIRI = factory.createIRI("https://schema.org/sameAs");
    private IRI softwareVersionIRI = factory.createIRI("https://schema.org/softwareVersion");
    private IRI dateModifiedIRI = factory.createIRI("https://schema.org/dateModified");

    //Review objects
    private  IRI reviewBodyIRI = factory.createIRI("https://schema.org/reviewBody");
    private  IRI datePublishedIRI = factory.createIRI("https://schema.org/datePublished");
    private  IRI authorIRI = factory.createIRI("https://schema.org/author");
    private  IRI reviewRatingIRI = factory.createIRI("https://schema.org/reviewRating");

    //Person objects
    private  IRI nameIRI = factory.createIRI("https://schema.org/name");

    //Feature object
    private IRI synonymIRI = factory.createIRI("https://schema.org/sameAs");

    public ApplicationRepository(final @Value("${db.url}") String url,
                                 final @Value("${db.username}") String username,
                                 final @Value("${db.password}") String password,
                                 final AppDataScannerService appDataScannerServ) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        appDataScannerService = appDataScannerServ;
    }
    private String findAllSimplifiedQuery(Integer page, Integer size) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name ?authorName ?reviewCount\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name  ?authorName\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:abstract ?abstract ;\n");
        queryBuilder.append("           schema:author ?author .\n");
        queryBuilder.append("      ?author schema:author ?authorName .\n");
        queryBuilder.append("    }\n");
        if (size != null) {
            queryBuilder.append("    LIMIT ").append(size).append("\n");
        }
        if (page != null && size != null) {
            int offset = (page - 1) * size;
            queryBuilder.append("    OFFSET ").append(offset).append("\n");
        }
        queryBuilder.append("  }\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name (STR(COUNT(?review)) as ?reviewCount)\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:review ?review .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    GROUP BY ?name\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  FILTER (?name = ?name)\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    private String findAllQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name ?description ?authorName ?reviewCount\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name ?description ?authorName\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:abstract ?abstract ;\n");
        queryBuilder.append("           schema:author ?author .\n");
        queryBuilder.append("      ?abstract schema:text ?description .\n");
        queryBuilder.append("      ?author schema:author ?authorName .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    LIMIT 20\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name (STR(COUNT(?review)) as ?reviewCount)\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:review ?review .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    GROUP BY ?name\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  FILTER (?name = ?name)\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    private String findAllApplicationNamesQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:name ?name ;\n");
        queryBuilder.append("       schema:identifier ?package .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    private String findByNameQuery(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT DISTINCT ?name\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:name ?name ;\n");
        queryBuilder.append("       schema:identifier ?package .\n");
        queryBuilder.append("    FILTER (STRSTARTS(STR(?name), \"").append(appName).append("\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("LIMIT 1");
        return queryBuilder.toString();
    }

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }
    private ApplicationSimplifiedDTO bindingSetToApplicationSimplifiedDTO(final BindingSet bindings) {
        ApplicationSimplifiedDTO applicationSimplifiedDTO = new ApplicationSimplifiedDTO();
        if (bindings.getBinding("name") != null
                && bindings.getBinding("name").getValue() != null) {
            applicationSimplifiedDTO.setName(
                    bindings.getBinding("name").getValue().stringValue());
        }
        if (bindings.getBinding("authorName") != null
                && bindings.getBinding("authorName").getValue() != null) {
            applicationSimplifiedDTO.setAuthor(
                    bindings.getBinding("authorName").getValue().stringValue());
        }
        if (bindings.getBinding("reviewCount") != null
                && bindings.getBinding("reviewCount").getValue() != null) {
            applicationSimplifiedDTO.setReviewCount(
                    Integer.valueOf(
                                    bindings.getBinding("reviewCount").getValue().stringValue()
                    )
            );
        }

        return applicationSimplifiedDTO;
    }

    private ApplicationDTO bindingSetToApplicationDTO(final BindingSet bindings) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        if (bindings.getBinding("name") != null
                && bindings.getBinding("name").getValue() != null) {
            applicationDTO.setName(String.valueOf(bindings.getBinding("name").getValue()));
        }
        return applicationDTO;
    }
    @Override
    public List<ApplicationDTO> findAll() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllQuery());
        List<ApplicationDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationDTO(result.next()));
        }
        return applicationDTOS;
    }

    private Model createEmptyModel() {
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
        return model;
    }

    public void insertApp(ApplicationDTO applicationDTO) {

        List<Statement> statements = new ArrayList<>();
        Model model = createEmptyModel();

        IRI devSubject = addDeveloperIntoStatements(applicationDTO, statements);

        IRI sub = factory.createIRI(appIRI + "/" + applicationDTO.getPackageName());
        statements.add(factory.createStatement(sub, typeIRI, appIRI));
        statements.add(factory.createStatement(sub, authorIRI, devSubject));

        //some fields are not populated, so we check them to avoid null pointers.
        //for (AppCategory category : app.getCategories()) {
        //    statements.add(factory.createStatement(sub, categoryIRI, factory.createLiteral(String.valueOf(category))));
        //}

        //if (app.getCategory() != null) {
        //    statements.add(factory.createStatement(sub, categoryIRI, factory.createLiteral(app.getCategory())));
        //}

        if (applicationDTO.getReleaseDate() != null) {
            statements.add(factory.createStatement(sub, datePublishedIRI, factory.createLiteral(applicationDTO.getReleaseDate())));
        }

        if (applicationDTO.getCurrentVersionReleaseDate() != null) {
            statements.add(factory.createStatement(sub, dateModifiedIRI, factory.createLiteral(applicationDTO.getCurrentVersionReleaseDate())));
        }

        if (applicationDTO.getVersion() != null) {
            statements.add(factory.createStatement(sub, softwareVersionIRI, factory.createLiteral(applicationDTO.getVersion())));
        }

        if (applicationDTO.getCategoryId() != null) {
            statements.add(factory.createStatement(sub, categoryIRI, factory.createLiteral(applicationDTO.getCategoryId())));
        }

        if (applicationDTO.getName() != null) {
            String sanitizedName = Utils.sanitizeString(applicationDTO.getName());
            statements.add(factory.createStatement(sub, nameIRI, factory.createLiteral(sanitizedName)));
        }
        if (applicationDTO.getPackageName() != null) statements.add(factory.createStatement(sub, identifierIRI, factory.createLiteral(applicationDTO.getPackageName())));

        if (applicationDTO.getDescription() != null) {
            addDigitalDocumentIntoStatements(applicationDTO.getPackageName(), applicationDTO.getDescription(), statements, sub, descriptionIRI, DocumentType.DESCRIPTION);
            //statements.add(factory.createStatement(sub, descriptionIRI, factory.createLiteral(app.getDescription())));
        }
        if (applicationDTO.getSummary() != null) {
            addDigitalDocumentIntoStatements(applicationDTO.getPackageName(), applicationDTO.getSummary(), statements, sub, summaryIRI, DocumentType.SUMMARY);
            //statements.add(factory.createStatement(sub, summaryIRI, factory.createLiteral(app.getSummary())));
        }
        /*
        if (applicationDTO.getChangelog() != null) {
            addDigitalDocument(applicationDTO.getPackageName(), applicationDTO.getChangelog(), statements, sub, changelogIRI, DocumentType.CHANGELOG);
            //statements.add(factory.createStatement(sub, changelogIRI, factory.createLiteral(app.getChangelog())));
        }*/
        //Adding reviewDocumentPlaceholder
        addDigitalDocumentIntoStatements(applicationDTO.getPackageName(), "Aggregated NL data for app " + applicationDTO.getName(), statements, sub, reviewDocumentIRI, DocumentType.REVIEWS);

        //Adding all reviews
        addReviews(applicationDTO, sub, statements);

        //EXTENDED KNOWLEDGE - Add features
        addFeatures(applicationDTO, sub, statements);

        //Committing all changes
        commitChanges(model, statements);
    }

    private void commitChanges(Model model, List<Statement> statements) {
        //model.addAll(statements);
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }

    private IRI addDeveloperIntoStatements(final ApplicationDTO applicationDTO,
                                           final List<Statement> statements) {
        String developerName = applicationDTO.getDeveloper().replace(" ","_");
        IRI devSubject = factory.createIRI(developerIRI+"/"+developerName);
        statements.add(factory.createStatement(devSubject,identifierIRI,factory.createLiteral(developerName)));
        statements.add(factory.createStatement(devSubject,authorIRI,factory.createLiteral(applicationDTO.getDeveloper())));
        if (applicationDTO.getDeveloperSite() != null) {
            statements.add(factory.createStatement(devSubject, sameAsIRI, factory.createLiteral(applicationDTO.getDeveloperSite())));
        }
        statements.add(factory.createStatement(devSubject, typeIRI, developerIRI));
        return devSubject;
    }

    private void addDigitalDocumentIntoStatements(final String packageName,
                                                  final String text,
                                                  final List<Statement> statements,
                                                  final IRI sub,
                                                  final IRI pred,
                                                  final DocumentType documentType) {
        IRI appDescription = factory.createIRI(digitalDocumentIRI + "/" + packageName + "-" + documentType);
        statements.add(factory.createStatement(appDescription, identifierIRI, factory.createLiteral(packageName + "-" + documentType)));
        statements.add(factory.createStatement(appDescription, textIRI, factory.createLiteral(text)));
        statements.add(factory.createStatement(appDescription, disambiguatingDescriptionIRI, factory.createLiteral(documentType.getName())));
        statements.add(factory.createStatement(sub, pred, appDescription));
        statements.add(factory.createStatement(appDescription, typeIRI, digitalDocumentIRI));
    }
    private void addReviews(ApplicationDTO applicationDTO, IRI sub, List<Statement> statements) {
        for (ReviewDTO r : applicationDTO.getReviewDTOS()) {
            IRI review = factory.createIRI(reviewIRI + "/" + r.getId());
            //normalize the text to utf-8 encoding
            String reviewBody = r.getBody();
            if (reviewBody != null) {
                byte[] reviewBytes = reviewBody.getBytes();
                String encoded_string = new String(reviewBytes, StandardCharsets.UTF_8);
                statements.add(factory.createStatement(review, reviewBodyIRI, factory.createLiteral(encoded_string)));
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
            statements.add(factory.createStatement(review, reviewRatingIRI, factory.createLiteral(r.getRating())));
            if (r.getPublished() != null) {
                statements.add(factory.createStatement(review, datePublishedIRI, factory.createLiteral(r.getPublished())));
            }
            statements.add(factory.createStatement(review, authorIRI, factory.createLiteral(r.getAuthor())));
            statements.add(factory.createStatement(sub, reviewsIRI, review));
            statements.add(factory.createStatement(review, typeIRI, reviewIRI));
            statements.add(factory.createStatement(review, identifierIRI, factory.createLiteral(r.getId())));
            //statements.add(factory.createStatement(author, typeIRI, personIRI));
        }
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

    public List<GraphApp> getAllApps() {
        List<GraphApp> apps = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select ?app ?identifier ?name where {\n" +
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

            GraphApp graphApp = new GraphApp(app.toString(), identifier, name);
            apps.add(graphApp);
        }

        return apps;
    }


    public void insert(final ApplicationDTO applicationDTO) {
        insertApp(applicationDTO);
    }

    public void update(int daysFromLastUpdate) {
        List<GraphApp> apps = getAllApps();
        for (GraphApp app : apps) {
            //We send requests app per app
            ApplicationDTO updatedApplicationDTO = appDataScannerService.scanApp(app, daysFromLastUpdate);

            if (updatedApplicationDTO != null) {
                insertApp(updatedApplicationDTO);
            }

            //TODO remove reviews older than MAX_DAYS_REVIEWS
        }
    }
    public List<ApplicationSimplifiedDTO> findAllSimplified() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllSimplifiedQuery(null, null));
        List<ApplicationSimplifiedDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationSimplifiedDTO(result.next()));
        }
        return applicationDTOS;
    }

    public List<ApplicationSimplifiedDTO> findAllSimplifiedPaginated(final Integer page,
                                                                     final Integer size) throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllSimplifiedQuery(page, size));
        List<ApplicationSimplifiedDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationSimplifiedDTO(result.next()));
        }
        return applicationDTOS;
    }

    public List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllApplicationNamesQuery());
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        List<ApplicationSimplifiedDTO> applicationDTOS = new ArrayList<>();
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationSimplifiedDTO(result.next()));
        }
        return applicationDTOS;
    }

    public ApplicationDTO findByName(final String appName) throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findByNameQuery(appName));
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found with the given app name");
        }

        return bindingSetToApplicationDTO(result.next());
    }
}
