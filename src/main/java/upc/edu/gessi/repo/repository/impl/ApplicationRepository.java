package upc.edu.gessi.repo.repository.impl;

import org.apache.commons.text.WordUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.*;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.RdfRepository;
import upc.edu.gessi.repo.service.impl.AppDataScannerService;
import upc.edu.gessi.repo.util.ApplicationQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ApplicationRepository <T> implements RdfRepository {

    private final HTTPRepository repository;

    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;
    private final AppDataScannerService appDataScannerService;

    private final ApplicationQueryBuilder applicationQueryBuilder;

    @Autowired
    public ApplicationRepository(final @Value("${db.url}") String url,
                                 final @Value("${db.username}") String username,
                                 final @Value("${db.password}") String password,
                                 final AppDataScannerService appDataScannerServ,
                                 final SchemaIRI schema,
                                 final ApplicationQueryBuilder appQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        appDataScannerService = appDataScannerServ;
        schemaIRI = schema;
        applicationQueryBuilder = appQB;
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

    private void commitChanges(final List<Statement> statements) {
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }

    @Override
    public List<ApplicationDTO> findAll() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllQuery());
        List<ApplicationDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationDTO(result.next()));
        }
        return applicationDTOS;
    }

    public void insertApp(ApplicationDTO applicationDTO) {
        List<Statement> statements = new ArrayList<>();

        IRI devSubject = addDeveloperIntoStatements(applicationDTO, statements);

        IRI sub = factory.createIRI(schemaIRI.getAppIRI() + "/" + applicationDTO.getPackageName());
        statements.add(factory.createStatement(sub, schemaIRI.getTypeIRI(), schemaIRI.getAppIRI()));
        statements.add(factory.createStatement(sub, schemaIRI.getAuthorIRI(), devSubject));

        if (applicationDTO.getCategoryId() != null) {
            statements.add(factory.createStatement(sub, schemaIRI.getCategoryIRI(), factory.createLiteral(applicationDTO.getCategoryId())));
        }
        if (applicationDTO.getCategory() != null) {
            statements.add(factory.createStatement(sub, schemaIRI.getCategoryIRI(), factory.createLiteral(applicationDTO.getCategory())));
        }
        for (String category : applicationDTO.getCategories()) {
            statements.add(factory.createStatement(sub, schemaIRI.getCategoryIRI(), factory.createLiteral(category)));
        }

        if (applicationDTO.getReleaseDate() != null) {
            statements.add(factory.createStatement(sub, schemaIRI.getDatePublishedIRI(), factory.createLiteral(applicationDTO.getReleaseDate())));
        }

        if (applicationDTO.getCurrentVersionReleaseDate() != null) {
            statements.add(factory.createStatement(sub, schemaIRI.getDateModifiedIRI(), factory.createLiteral(applicationDTO.getCurrentVersionReleaseDate())));
        }

        if (applicationDTO.getVersion() != null) {
            statements.add(factory.createStatement(sub, schemaIRI.getSoftwareVersionIRI(), factory.createLiteral(applicationDTO.getVersion())));
        }

        if (applicationDTO.getName() != null) {
            String sanitizedName = Utils.sanitizeString(applicationDTO.getName());
            statements.add(factory.createStatement(sub, schemaIRI.getNameIRI(), factory.createLiteral(sanitizedName)));
        }
        if (applicationDTO.getPackageName() != null) {
            statements.add(factory.createStatement(sub, schemaIRI.getIdentifierIRI(), factory.createLiteral(applicationDTO.getPackageName())));
        }

        if (applicationDTO.getDescription() != null) {
            addDigitalDocumentIntoStatements(
                    applicationDTO.getPackageName(),
                    applicationDTO.getDescription(),
                    statements,
                    sub,
                    schemaIRI.getDescriptionIRI(),
                    DocumentType.DESCRIPTION);
            //statements.add(factory.createStatement(sub, descriptionIRI, factory.createLiteral(app.getDescription())));
        }
        if (applicationDTO.getSummary() != null) {
            addDigitalDocumentIntoStatements(
                    applicationDTO.getPackageName(),
                    applicationDTO.getSummary(),
                    statements,
                    sub,
                    schemaIRI.getSummaryIRI(),
                    DocumentType.SUMMARY);
            //statements.add(factory.createStatement(sub, summaryIRI, factory.createLiteral(app.getSummary())));
        }

        if (applicationDTO.getChangelog() != null) {
            addDigitalDocumentIntoStatements(
                    applicationDTO.getPackageName(),
                    applicationDTO.getChangelog(),
                    statements,
                    sub,
                    schemaIRI.getChangelogIRI(),
                    DocumentType.CHANGELOG);
            //statements.add(factory.createStatement(sub, changelogIRI, factory.createLiteral(app.getChangelog())));
        }
        //Adding reviewDocumentPlaceholder
        addDigitalDocumentIntoStatements(
                applicationDTO.getPackageName(),
                "Aggregated NL data for app " + applicationDTO.getName(),
                statements,
                sub,
                schemaIRI.getReviewDocumentIRI(),
                DocumentType.REVIEWS);

        addReviews(applicationDTO, sub, statements);

        addFeaturesToApplication(applicationDTO, sub, statements);

        commitChanges(statements);
    }


    private IRI addDeveloperIntoStatements(final ApplicationDTO applicationDTO,
                                           final List<Statement> statements) {
        String developerName = applicationDTO.getDeveloper().replace(" ","_");
        IRI devSubject = factory.createIRI(schemaIRI.getDeveloperIRI()+"/"+developerName);
        statements.add(
                factory.createStatement(
                        devSubject, schemaIRI.getIdentifierIRI(), factory.createLiteral(developerName)));
        statements.add(
                factory.createStatement(
                        devSubject, schemaIRI.getAuthorIRI(), factory.createLiteral(applicationDTO.getDeveloper())));
        if (applicationDTO.getDeveloperSite() != null) {
            statements.add(factory.createStatement(devSubject, schemaIRI.getSameAsIRI(), factory.createLiteral(applicationDTO.getDeveloperSite())));
        }
        statements.add(factory.createStatement(devSubject, schemaIRI.getTypeIRI(), schemaIRI.getDeveloperIRI()));
        return devSubject;
    }

    private void addDigitalDocumentIntoStatements(final String packageName,
                                                  final String text,
                                                  final List<Statement> statements,
                                                  final IRI sub,
                                                  final IRI pred,
                                                  final DocumentType documentType) {
        IRI appDescription = factory.createIRI(schemaIRI.getDigitalDocumentIRI() + "/" + packageName + "-" + documentType);
        statements.add(factory.createStatement(appDescription, schemaIRI.getIdentifierIRI(), factory.createLiteral(packageName + "-" + documentType)));
        statements.add(factory.createStatement(appDescription, schemaIRI.getTextIRI(), factory.createLiteral(text)));
        statements.add(factory.createStatement(appDescription, schemaIRI.getDisambiguatingDescriptionIRI(), factory.createLiteral(documentType.getName())));
        statements.add(factory.createStatement(sub, pred, appDescription));
        statements.add(factory.createStatement(appDescription, schemaIRI.getTypeIRI(), schemaIRI.getDigitalDocumentIRI()));
    }
    private void addReviews(ApplicationDTO applicationDTO, IRI sub, List<Statement> statements) {
        for (ReviewDTO r : applicationDTO.getReviewDTOS()) {
            IRI review = factory.createIRI(schemaIRI.getReviewIRI() + "/" + r.getId());
            //normalize the text to utf-8 encoding
            String reviewBody = r.getBody();
            if (reviewBody != null) {
                byte[] reviewBytes = reviewBody.getBytes();
                String encoded_string = new String(reviewBytes, StandardCharsets.UTF_8);
                statements.add(factory.createStatement(review, schemaIRI.getReviewBodyIRI(), factory.createLiteral(encoded_string)));
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

    public void addFeaturesToApplication(ApplicationDTO applicationDTO, IRI sub, List<Statement> statements) {
        for (String feature : applicationDTO.getFeatures()) {
            String id = WordUtils.capitalize(feature).replace(" ", "").replaceAll("[^a-zA-Z0-9]", "");
            IRI featureIRI = factory.createIRI(schemaIRI.getDefinedTermIRI() + "/" + id);
            statements.add(factory.createStatement(featureIRI, schemaIRI.getNameIRI(), factory.createLiteral(feature)));
            statements.add(factory.createStatement(featureIRI,  schemaIRI.getIdentifierIRI(), factory.createLiteral(id)));
            statements.add(factory.createStatement(sub,  schemaIRI.getFeaturesIRI(), featureIRI));
            statements.add(factory.createStatement(featureIRI,  schemaIRI.getTypeIRI(),  schemaIRI.getDefinedTermIRI()));
        }
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
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllSimplifiedQuery(null, null));
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
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllSimplifiedQuery(page, size));
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
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllApplicationNamesQuery());
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
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findByNameQuery(appName));
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found with the given app name");
        }

        return bindingSetToApplicationDTO(result.next());
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
