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
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.RdfRepository;
import upc.edu.gessi.repo.service.impl.AppDataScannerServiceImpl;
import upc.edu.gessi.repo.service.impl.ReviewService;
import upc.edu.gessi.repo.util.ApplicationQueryBuilder;
import upc.edu.gessi.repo.util.ReviewQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class MobileApplicationRepository<T> implements RdfRepository {

    private final HTTPRepository repository;

    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;
    private final AppDataScannerServiceImpl appDataScannerServiceImpl;

    private final ApplicationQueryBuilder applicationQueryBuilder;

    private final ReviewService reviewService;
    private final ReviewQueryBuilder reviewQueryBuilder;
    @Autowired
    public MobileApplicationRepository(final @Value("${db.url}") String url,
                                       final @Value("${db.username}") String username,
                                       final @Value("${db.password}") String password,
                                       final AppDataScannerServiceImpl appDataScannerServ,
                                       final SchemaIRI schema,
                                       final ApplicationQueryBuilder appQB,
                                       final ReviewQueryBuilder reviewQB,
                                       final ReviewService reviewSv) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        appDataScannerServiceImpl = appDataScannerServ;
        schemaIRI = schema;
        reviewService = reviewSv;
        applicationQueryBuilder = appQB;
        reviewQueryBuilder = reviewQB;
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
        if (bindings.getBinding("package") != null
                && bindings.getBinding("package").getValue() != null) {
            applicationSimplifiedDTO.setApplicationPackage(
                    bindings.getBinding("package").getValue().stringValue());
        }
        if (bindings.getBinding("reviewCount") != null
                && bindings.getBinding("reviewCount").getValue() != null) {
            applicationSimplifiedDTO.setReviewCount(
                    Integer.valueOf(bindings.getBinding("reviewCount").getValue().stringValue()));
        }
        return applicationSimplifiedDTO;
    }

    private MobileApplicationDTO bindingSetToApplicationDTO(final TupleQueryResult result) {
        MobileApplicationDTO mobileApplicationDTO = new MobileApplicationDTO();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if (existsPredicateAndObject(bindings)) {
                String predicateValue = bindings.getBinding("predicate").getValue().stringValue();
                String identifierIRI = schemaIRI.getIdentifierIRI().stringValue();
                String authorIRI = schemaIRI.getAuthorIRI().stringValue();
                String datePublishedIRI = schemaIRI.getDatePublishedIRI().stringValue();
                String versionIRI = schemaIRI.getSoftwareVersionIRI().stringValue();
                String categoryIRI = schemaIRI.getCategoryIRI().stringValue();
                String reviewIRI = schemaIRI.getReviewsIRI().stringValue();
                String nameIRI = schemaIRI.getNameIRI().stringValue();
                if (predicateValue.equals(identifierIRI)) {
                    mobileApplicationDTO.setPackageName(bindings
                            .getBinding("object")
                            .getValue()
                            .stringValue());
                } else if (predicateValue.equals(authorIRI)) {
                    String auth = bindings
                            .getBinding("object")
                            .getValue()
                            .stringValue()
                            .split("https://schema.org/Organization/")[1];
                    mobileApplicationDTO.setDeveloper(auth);
                } else if (predicateValue.equals(datePublishedIRI)) {
                    String dateTimeString = bindings.getBinding("object").getValue().stringValue();
                    String datePart = dateTimeString.substring(0, 10);
                    Date sqlDate = Date.valueOf(datePart);
                    mobileApplicationDTO.setReleaseDate(sqlDate);
                } else if (predicateValue.equals(versionIRI)) {
                    mobileApplicationDTO.setVersion(bindings.getBinding("object").getValue().stringValue());
                } else if (predicateValue.equals(categoryIRI)) {
                    mobileApplicationDTO.getCategories().add(bindings.getBinding("object").getValue().stringValue());
                } else if (predicateValue.equals(reviewIRI)) {
                    String reviewId = bindings
                            .getBinding("object")
                            .getValue()
                            .stringValue()
                            .split("https://schema.org/Review/")[1];
                    ReviewDTO reviewResponseDTO = new ReviewDTO();
                    reviewResponseDTO.setId(reviewId);
                    List<String> reviewIds = new ArrayList<>(Collections.singleton(reviewResponseDTO.getId()));

                    TupleQueryResult textResult =
                            runSparqlQuery(reviewQueryBuilder.findTextReviewsQuery(reviewIds));
                    if (textResult.hasNext()) {
                        BindingSet bindingSet = textResult.next();
                        if (bindingSet.getBinding("text") != null
                                && bindingSet.getBinding("text").getValue().stringValue() != null) {
                            reviewResponseDTO.setReviewText(bindingSet.getBinding("text").getValue().stringValue());
                        }
                    }

                    reviewResponseDTO.setSentences(new ArrayList<>());
                    TupleQueryResult sentencesResult =
                            runSparqlQuery(reviewQueryBuilder.findReviewSentencesEmotions(new ArrayList<>(Collections.singleton(reviewResponseDTO.getId()))));
                    while (sentencesResult.hasNext()) {
                        reviewResponseDTO
                                .getSentences()
                                .add(reviewService.getSentenceDTO(sentencesResult));
                    }
                    mobileApplicationDTO.getReviewDTOS().add(reviewResponseDTO);
                } else if (predicateValue.equals(nameIRI)) {
                    mobileApplicationDTO.setAppName(bindings.getBinding("object").getValue().stringValue());
                }
            }
        }
        return mobileApplicationDTO;
    }



    private boolean existsPredicateAndObject(BindingSet bindings) {
        return bindings.getBinding("predicate") != null
                && bindings.getBinding("predicate").getValue() != null
                && bindings.getBinding("object") != null
                && bindings.getBinding("object").getValue() != null;
    }


    private void commitChanges(final List<Statement> statements) {
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(statements);
        repoConnection.close();
    }

    @Override
    public List<MobileApplicationDTO> findAll() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllQuery());
        List<MobileApplicationDTO> mobileApplicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        mobileApplicationDTOS.add(bindingSetToApplicationDTO(result));
        return mobileApplicationDTOS;
    }

    public MobileApplicationDTO insertApp(MobileApplicationDTO mobileApplicationDTO) {
        List<Statement> statements = new ArrayList<>();

        IRI applicationIRI = factory.createIRI(schemaIRI.getAppIRI() + "/" + mobileApplicationDTO.getPackageName());
        statements.add(factory.createStatement(applicationIRI, schemaIRI.getTypeIRI(), schemaIRI.getAppIRI()));

        if (mobileApplicationDTO.getDeveloper() != null) {
            IRI devSubject = addDeveloperIntoStatements(mobileApplicationDTO, statements);
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getAuthorIRI(), devSubject));
        }

        if (mobileApplicationDTO.getCategoryId() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getCategoryIRI(), factory.createLiteral(mobileApplicationDTO.getCategoryId())));
        }
        if (mobileApplicationDTO.getCategory() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getCategoryIRI(), factory.createLiteral(mobileApplicationDTO.getCategory())));
        }
        if (mobileApplicationDTO.getCategories() != null) {
            for (String category : mobileApplicationDTO.getCategories()) {
                statements.add(factory.createStatement(applicationIRI, schemaIRI.getCategoryIRI(), factory.createLiteral(category)));
            }
        }
        if (mobileApplicationDTO.getReleaseDate() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getDatePublishedIRI(), factory.createLiteral(mobileApplicationDTO.getReleaseDate())));
        }

        if (mobileApplicationDTO.getCurrentVersionReleaseDate() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getDateModifiedIRI(), factory.createLiteral(mobileApplicationDTO.getCurrentVersionReleaseDate())));
        }

        if (mobileApplicationDTO.getVersion() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getSoftwareVersionIRI(), factory.createLiteral(mobileApplicationDTO.getVersion())));
        }

        if (mobileApplicationDTO.getAppName() != null) {
            String sanitizedName = Utils.sanitizeString(mobileApplicationDTO.getAppName());
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getNameIRI(), factory.createLiteral(sanitizedName)));
        }
        if (mobileApplicationDTO.getPackageName() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(mobileApplicationDTO.getPackageName())));
        }

        if (mobileApplicationDTO.getDescription() != null) {
                addDigitalDocumentIntoStatements(
                    mobileApplicationDTO.getPackageName(),
                    mobileApplicationDTO.getDescription(),
                    statements,
                    applicationIRI,
                    schemaIRI.getDescriptionIRI(),
                    DocumentType.DESCRIPTION);
            //statements.add(factory.createStatement(sub, descriptionIRI, factory.createLiteral(app.getDescription())));
        }
        if (mobileApplicationDTO.getSummary() != null) {
            addDigitalDocumentIntoStatements(
                    mobileApplicationDTO.getPackageName(),
                    mobileApplicationDTO.getSummary(),
                    statements,
                    applicationIRI,
                    schemaIRI.getSummaryIRI(),
                    DocumentType.SUMMARY);
            //statements.add(factory.createStatement(sub, summaryIRI, factory.createLiteral(app.getSummary())));
        }

        if (mobileApplicationDTO.getChangelog() != null) {
            addDigitalDocumentIntoStatements(
                    mobileApplicationDTO.getPackageName(),
                    mobileApplicationDTO.getChangelog(),
                    statements,
                    applicationIRI,
                    schemaIRI.getChangelogIRI(),
                    DocumentType.CHANGELOG);
            //statements.add(factory.createStatement(sub, changelogIRI, factory.createLiteral(app.getChangelog())));
        }

        addDigitalDocumentIntoStatements(
                mobileApplicationDTO.getPackageName(),
                "Aggregated NL data for app " + mobileApplicationDTO.getAppName(),
                statements,
                applicationIRI,
                schemaIRI.getReviewDocumentIRI(),
                DocumentType.REVIEWS);

        if (mobileApplicationDTO.getFeatures() != null) {
            addFeaturesToApplication(mobileApplicationDTO, applicationIRI, statements);
        }
        reviewService.addCompleteReviewsToApplication(mobileApplicationDTO, applicationIRI, statements);

        commitChanges(statements);

        return mobileApplicationDTO;
    }


    private IRI addDeveloperIntoStatements(final MobileApplicationDTO completeApplicationDataDTO,
                                           final List<Statement> statements) {

        String developerName = completeApplicationDataDTO.getDeveloper().replace(" ","_");
        IRI devSubject = factory.createIRI(schemaIRI.getDeveloperIRI()+"/"+developerName);
        statements.add(
                factory.createStatement(
                        devSubject, schemaIRI.getIdentifierIRI(), factory.createLiteral(developerName)));
        statements.add(
                factory.createStatement(
                        devSubject, schemaIRI.getAuthorIRI(), factory.createLiteral(completeApplicationDataDTO.getDeveloper())));
        if (completeApplicationDataDTO.getDeveloperSite() != null) {
            statements.add(factory.createStatement(devSubject, schemaIRI.getSameAsIRI(), factory.createLiteral(completeApplicationDataDTO.getDeveloperSite())));
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
        statements.add(factory.createStatement(appDescription, schemaIRI.getTextPropertyIRI(), factory.createLiteral(text)));
        statements.add(factory.createStatement(appDescription, schemaIRI.getDisambiguatingDescriptionIRI(), factory.createLiteral(documentType.getName())));
        statements.add(factory.createStatement(sub, pred, appDescription));
        statements.add(factory.createStatement(appDescription, schemaIRI.getTypeIRI(), schemaIRI.getDigitalDocumentIRI()));
    }

    public void addFeaturesToApplication(final MobileApplicationDTO completeApplicationDataDTO,
                                         final IRI sub,
                                         final List<Statement> statements) {
        for (String feature : completeApplicationDataDTO.getFeatures()) {
            String id = WordUtils.capitalize(feature).replace(" ", "").replaceAll("[^a-zA-Z0-9]", "");
            IRI featureIRI = factory.createIRI(schemaIRI.getDefinedTermIRI() + "/" + id);
            statements.add(factory.createStatement(featureIRI, schemaIRI.getNameIRI(), factory.createLiteral(feature)));
            statements.add(factory.createStatement(featureIRI,  schemaIRI.getIdentifierIRI(), factory.createLiteral(id)));
            statements.add(factory.createStatement(sub,  schemaIRI.getKeywordIRI(), featureIRI));
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
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

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


    public void insert(final MobileApplicationDTO completeApplicationDataDTO) {
        insertApp(completeApplicationDataDTO);
    }

    public void update(int daysFromLastUpdate) {
        List<GraphApp> apps = getAllApps();
        for (GraphApp app : apps) {
            //We send requests app per app
            MobileApplicationDTO updatedCompleteApplicationDataDTO = appDataScannerServiceImpl.scanApp(app, daysFromLastUpdate);

            if (updatedCompleteApplicationDataDTO != null) {
                insertApp(updatedCompleteApplicationDataDTO);
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

    public MobileApplicationDTO findByName(final String appName) throws ObjectNotFoundException {
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findByNameQuery(appName));
        if (!result.hasNext()) {
            throw new ObjectNotFoundException("No applications were found with the given app name");
        }
        return bindingSetToApplicationDTO(result);
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

}