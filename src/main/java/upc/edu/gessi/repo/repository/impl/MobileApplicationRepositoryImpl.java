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
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.MobileApplicationRepository;
import upc.edu.gessi.repo.service.impl.AppDataScannerServiceImpl;
import upc.edu.gessi.repo.service.impl.ReviewServiceImpl;
import upc.edu.gessi.repo.util.ApplicationQueryBuilder;
import upc.edu.gessi.repo.util.ReviewQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class MobileApplicationRepositoryImpl implements MobileApplicationRepository {

    private final HTTPRepository repository;

    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;
    private final AppDataScannerServiceImpl appDataScannerServiceImpl;

    private final ApplicationQueryBuilder applicationQueryBuilder;

    private final ReviewServiceImpl reviewServiceImpl;
    private final ReviewQueryBuilder reviewQueryBuilder;
    @Autowired
    public MobileApplicationRepositoryImpl(final @Value("${db.url}") String url,
                                           final @Value("${db.username}") String username,
                                           final @Value("${db.password}") String password,
                                           final AppDataScannerServiceImpl appDataScannerServ,
                                           final SchemaIRI schema,
                                           final ApplicationQueryBuilder appQB,
                                           final ReviewQueryBuilder reviewQB,
                                           final ReviewServiceImpl reviewSv) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        appDataScannerServiceImpl = appDataScannerServ;
        schemaIRI = schema;
        reviewServiceImpl = reviewSv;
        applicationQueryBuilder = appQB;
        reviewQueryBuilder = reviewQB;
    }

    @Override
    public List<MobileApplicationFullDataDTO> findAll() throws MobileApplicationNotFoundException {
        List<BindingSet> bindingSetResults = runSparqlQuery(applicationQueryBuilder.findAllQuery()).stream().toList();
        List<MobileApplicationFullDataDTO> mobileApplicationFullDataDTOS = new ArrayList<>();

        String currentAppUri = null;
        MobileApplicationFullDataDTO mobileApplicationFullDataDTO = null;
        for (BindingSet bs : bindingSetResults) {
            String appUri = bs.getBinding("subject").getValue().stringValue();
            if (!appUri.equals(currentAppUri)) {
                if (mobileApplicationFullDataDTO != null) {
                    mobileApplicationFullDataDTOS.add(mobileApplicationFullDataDTO);
                }
                currentAppUri = appUri;
                mobileApplicationFullDataDTO = new MobileApplicationFullDataDTO();
            }
            updateMobileAppFromBindings(mobileApplicationFullDataDTO, bs);

        }
        if (mobileApplicationFullDataDTOS.isEmpty()) {
            throw new MobileApplicationNotFoundException("No applications were found");
        }

        return mobileApplicationFullDataDTOS;
    }

    @Override
    public MobileApplicationFullDataDTO insert(MobileApplicationFullDataDTO mobileApplicationFullDataDTO) {
        List<Statement> statements = new ArrayList<>();

        IRI applicationIRI = factory.createIRI(schemaIRI.getAppIRI() + "/" + mobileApplicationFullDataDTO.getPackageName());
        statements.add(factory.createStatement(applicationIRI, schemaIRI.getTypeIRI(), schemaIRI.getAppIRI()));

        addDeveloperToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addCategoriesToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addReleaseDateToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addCurrentVersionReleaseToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addVersionToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addAppNameToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addPackageNameToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addDescriptionToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addSummaryToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addChangelogToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addReviewDocumentToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addFeaturesToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);
        addReviewsToStatements(mobileApplicationFullDataDTO, statements, applicationIRI);

        commitChanges(statements);

        return mobileApplicationFullDataDTO;
    }

    @Override
    public MobileApplicationFullDataDTO update(MobileApplicationFullDataDTO entity) {
        if (entity != null) {
            insert(entity);
        }
        return entity;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void addFeature(final MobileApplicationFullDataDTO completeApplicationDataDTO,
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

    @Override
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


    public void update(int daysFromLastUpdate) {
        List<GraphApp> apps = getAllApps();
        for (GraphApp app : apps) {

            MobileApplicationFullDataDTO updatedCompleteApplicationDataDTO = appDataScannerServiceImpl.scanApp(app, daysFromLastUpdate);

            if (updatedCompleteApplicationDataDTO != null) {
                insert(updatedCompleteApplicationDataDTO);
            }
        }
    }


    public List<MobileApplicationFullDataDTO> findAllPaginated(final Integer page,
                                                                final Integer size) throws MobileApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllPaginatedQuery(page, size));
        List<MobileApplicationFullDataDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new MobileApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(queryResultToMobileApplicationFullDataDTO(result));
        }
        return applicationDTOS;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> findAllBasicDataPaginated(final Integer page, final Integer size) throws MobileApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllPaginatedSimplifiedQuery(page, size));
        List<MobileApplicationBasicDataDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new MobileApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToMobileApplicationBasicDataDTO(result.next()));
        }
        return applicationDTOS;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> findAllApplicationsBasicData() throws MobileApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findAllApplicationNamesQuery());
        if (!result.hasNext()) {
            throw new MobileApplicationNotFoundException("No applications were found");
        }
        List<MobileApplicationBasicDataDTO> applicationDTOS = new ArrayList<>();
        while (result.hasNext()) {
            MobileApplicationFullDataDTO fullDataDTO = queryResultToMobileApplicationFullDataDTO(result);
            MobileApplicationBasicDataDTO basicDataDTO = new MobileApplicationBasicDataDTO();
            basicDataDTO.setAppName(fullDataDTO.getAppName());
            basicDataDTO.setPackageName(fullDataDTO.getPackageName());
            basicDataDTO.setReviewCount(fullDataDTO.getReviewCount());
            applicationDTOS.add(basicDataDTO);
        }
        return applicationDTOS;
    }

    @Override
    public MobileApplicationFullDataDTO findByName(final String appName) throws ObjectNotFoundException {
        TupleQueryResult result = runSparqlQuery(applicationQueryBuilder.findByNameQuery(appName));
        if (!result.hasNext()) {
            throw new ObjectNotFoundException("No applications were found with the given app name");
        }
        return queryResultToMobileApplicationFullDataDTO(result);
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

    private void addReviewsToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        reviewServiceImpl.addCompleteReviewsToApplication(mobileApplicationFullDataDTO, applicationIRI, statements);
    }

    private void addFeaturesToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getFeatures() != null) {
            addFeature(mobileApplicationFullDataDTO, applicationIRI, statements);
        }
    }

    private void addReviewDocumentToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        addDigitalDocument(
                mobileApplicationFullDataDTO.getPackageName(),
                "Aggregated NL data for app " + mobileApplicationFullDataDTO.getAppName(),
                statements,
                applicationIRI,
                schemaIRI.getReviewDocumentIRI(),
                DocumentType.REVIEWS);
    }

    private void addChangelogToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getChangelog() != null) {
            addDigitalDocument(
                    mobileApplicationFullDataDTO.getPackageName(),
                    mobileApplicationFullDataDTO.getChangelog(),
                    statements,
                    applicationIRI,
                    schemaIRI.getChangelogIRI(),
                    DocumentType.CHANGELOG);
            //statements.add(factory.createStatement(sub, changelogIRI, factory.createLiteral(app.getChangelog())));
        }
    }

    private void addSummaryToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getSummary() != null) {
            addDigitalDocument(
                    mobileApplicationFullDataDTO.getPackageName(),
                    mobileApplicationFullDataDTO.getSummary(),
                    statements,
                    applicationIRI,
                    schemaIRI.getSummaryIRI(),
                    DocumentType.SUMMARY);
            //statements.add(factory.createStatement(sub, summaryIRI, factory.createLiteral(app.getSummary())));
        }
    }

    private void addDescriptionToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getDescription() != null) {
            addDigitalDocument(
                    mobileApplicationFullDataDTO.getPackageName(),
                    mobileApplicationFullDataDTO.getDescription(),
                    statements,
                    applicationIRI,
                    schemaIRI.getDescriptionIRI(),
                    DocumentType.DESCRIPTION);
            //statements.add(factory.createStatement(sub, descriptionIRI, factory.createLiteral(app.getDescription())));
        }
    }

    private void addPackageNameToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getPackageName() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getIdentifierIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getPackageName())));
        }
    }

    private void addAppNameToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getAppName() != null) {
            String sanitizedName = Utils.sanitizeString(mobileApplicationFullDataDTO.getAppName());
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getNameIRI(), factory.createLiteral(sanitizedName)));
        }
    }

    private void addVersionToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getVersion() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getSoftwareVersionIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getVersion())));
        }
    }

    private void addCurrentVersionReleaseToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getCurrentVersionReleaseDate() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getDateModifiedIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getCurrentVersionReleaseDate())));
        }
    }

    private void addReleaseDateToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getReleaseDate() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getDatePublishedIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getReleaseDate())));
        }
    }

    private void addCategoriesToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getCategoryId() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getCategoryIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getCategoryId())));
        }
        if (mobileApplicationFullDataDTO.getCategory() != null) {
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getCategoryIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getCategory())));
        }
        if (mobileApplicationFullDataDTO.getCategories() != null) {
            for (String category : mobileApplicationFullDataDTO.getCategories()) {
                statements.add(factory.createStatement(applicationIRI, schemaIRI.getCategoryIRI(), factory.createLiteral(category)));
            }
        }
    }

    private void addDeveloperToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, List<Statement> statements, IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getDeveloper() != null) {
            IRI devSubject = createDeveloperIRI(mobileApplicationFullDataDTO, statements);
            statements.add(factory.createStatement(applicationIRI, schemaIRI.getAuthorIRI(), devSubject));
        }
    }


    private IRI createDeveloperIRI(final MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                                   final List<Statement> statements) {

        String developerName = mobileApplicationFullDataDTO.getDeveloper().replace(" ","_");
        IRI devSubject = factory.createIRI(schemaIRI.getDeveloperIRI()+"/"+developerName);
        statements.add(
                factory.createStatement(
                        devSubject, schemaIRI.getIdentifierIRI(), factory.createLiteral(developerName)));
        statements.add(
                factory.createStatement(
                        devSubject, schemaIRI.getAuthorIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getDeveloper())));
        if (mobileApplicationFullDataDTO.getDeveloperSite() != null) {
            statements.add(factory.createStatement(devSubject, schemaIRI.getSameAsIRI(), factory.createLiteral(mobileApplicationFullDataDTO.getDeveloperSite())));
        }
        statements.add(factory.createStatement(devSubject, schemaIRI.getTypeIRI(), schemaIRI.getDeveloperIRI()));
        return devSubject;
    }

    private void addDigitalDocument(final String packageName,
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

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

    private MobileApplicationBasicDataDTO bindingSetToMobileApplicationBasicDataDTO(final BindingSet bindings) {
        MobileApplicationBasicDataDTO mobileApplicationBasicDataDTO = new MobileApplicationBasicDataDTO();
        if (bindings.getBinding("name") != null
                && bindings.getBinding("name").getValue() != null) {
            mobileApplicationBasicDataDTO.setAppName(
                    bindings.getBinding("name").getValue().stringValue());
        }
        if (bindings.getBinding("package") != null
                && bindings.getBinding("package").getValue() != null) {
            mobileApplicationBasicDataDTO.setPackageName(
                    bindings.getBinding("package").getValue().stringValue());
        }
        if (bindings.getBinding("reviewCount") != null
                && bindings.getBinding("reviewCount").getValue() != null) {
            mobileApplicationBasicDataDTO.setReviewCount(
                    Integer.valueOf(bindings.getBinding("reviewCount").getValue().stringValue()));
        }
        return mobileApplicationBasicDataDTO;
    }

    private MobileApplicationFullDataDTO queryResultToMobileApplicationFullDataDTO(final TupleQueryResult result) {
        MobileApplicationFullDataDTO mobileApplicationFullDataDTO = new MobileApplicationFullDataDTO();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if (existsPredicateAndObject(bindings)) {
                updateMobileAppFromBindings(mobileApplicationFullDataDTO, bindings);
            }
        }
        return mobileApplicationFullDataDTO;
    }

    private void updateMobileAppFromBindings(MobileApplicationFullDataDTO mobileApplicationFullDataDTO, BindingSet bindings) {
        String predicateValue = bindings.getBinding("predicate").getValue().stringValue();
        String identifierIRI = schemaIRI.getIdentifierIRI().stringValue();
        String authorIRI = schemaIRI.getAuthorIRI().stringValue();
        String datePublishedIRI = schemaIRI.getDatePublishedIRI().stringValue();
        String versionIRI = schemaIRI.getSoftwareVersionIRI().stringValue();
        String categoryIRI = schemaIRI.getCategoryIRI().stringValue();
        String reviewIRI = schemaIRI.getReviewsIRI().stringValue();
        String nameIRI = schemaIRI.getNameIRI().stringValue();
        if (predicateValue.equals(identifierIRI)) {
            mobileApplicationFullDataDTO.setPackageName(bindings
                    .getBinding("object")
                    .getValue()
                    .stringValue());
        } else if (predicateValue.equals(authorIRI)) {
            String auth = bindings
                    .getBinding("object")
                    .getValue()
                    .stringValue()
                    .split("https://schema.org/Organization/")[1];
            mobileApplicationFullDataDTO.setDeveloper(auth);
        } else if (predicateValue.equals(datePublishedIRI)) {
            String dateTimeString = bindings.getBinding("object").getValue().stringValue();
            String datePart = dateTimeString.substring(0, 10);
            Date sqlDate = Date.valueOf(datePart);
            mobileApplicationFullDataDTO.setReleaseDate(sqlDate);
        } else if (predicateValue.equals(versionIRI)) {
            mobileApplicationFullDataDTO.setVersion(bindings.getBinding("object").getValue().stringValue());
        } else if (predicateValue.equals(categoryIRI)) {
            mobileApplicationFullDataDTO.getCategories().add(bindings.getBinding("object").getValue().stringValue());
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
                    runSparqlQuery(
                            reviewQueryBuilder
                                    .findReviewSentencesEmotions(
                                            new ArrayList<>(Collections.singleton(reviewResponseDTO.getId()))));
            while (sentencesResult.hasNext()) {
                reviewResponseDTO
                        .getSentences()
                        .add(reviewServiceImpl.getSentenceDTO(sentencesResult));
            }
            mobileApplicationFullDataDTO.getReviews().add(reviewResponseDTO);
        } else if (predicateValue.equals(nameIRI)) {
            mobileApplicationFullDataDTO.setAppName(bindings.getBinding("object").getValue().stringValue());
        }
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

}