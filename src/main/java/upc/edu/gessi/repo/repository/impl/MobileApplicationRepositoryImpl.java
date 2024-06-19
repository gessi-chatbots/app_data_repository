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
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.MobileApplications.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.exception.MobileApplications.NoMobileApplicationsFoundException;
import upc.edu.gessi.repo.repository.MobileApplicationRepository;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.util.*;

import java.sql.Date;
import java.util.*;

@Repository
public class MobileApplicationRepositoryImpl implements MobileApplicationRepository {

    private final HTTPRepository repository;

    private final ValueFactory factory = SimpleValueFactory.getInstance();

    private final SchemaIRI schemaIRI;

    private final ReviewRepository reviewRepository;

    private final MobileApplicationsQueryBuilder mobileApplicationsQueryBuilder;

    private final ReviewQueryBuilder reviewQueryBuilder;
    @Autowired
    public MobileApplicationRepositoryImpl(final @Value("${db.url}") String url,
                                           final @Value("${db.username}") String username,
                                           final @Value("${db.password}") String password,
                                           final SchemaIRI schema,
                                           final ReviewRepository revRepo,
                                           final MobileApplicationsQueryBuilder appQB,
                                           final ReviewQueryBuilder reviewQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        schemaIRI = schema;
        reviewRepository = revRepo;
        mobileApplicationsQueryBuilder = appQB;
        reviewQueryBuilder = reviewQB;

    }

    @Override
    public List<MobileApplicationFullDataDTO> findAll() throws NoMobileApplicationsFoundException {
        List<BindingSet> bindingSetResults = runSparqlQuery(mobileApplicationsQueryBuilder.findAllQuery()).stream().toList();
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
            throw new NoMobileApplicationsFoundException("No Mobile Applications were found");
        }

        return mobileApplicationFullDataDTOS;
    }

    @Override
    public IRI insert(MobileApplicationFullDataDTO mobileApplicationFullDataDTO) {
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
        commitChanges(statements);
        return applicationIRI;

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
        Utils.runSparqlUpdateQuery(repository.getConnection(),
                mobileApplicationsQueryBuilder.deleteByNameQuery(id));
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





    public List<MobileApplicationFullDataDTO> findAllPaginated(final Integer page,
                                                                final Integer size) throws NoMobileApplicationsFoundException {
        TupleQueryResult result = runSparqlQuery(mobileApplicationsQueryBuilder.findAllPaginatedQuery(page, size));
        List<MobileApplicationFullDataDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new NoMobileApplicationsFoundException("No Mobile Applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(queryResultToMobileApplicationFullDataDTO(result));
        }
        return applicationDTOS;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> findAllBasicDataPaginated(final Integer page, final Integer size) throws NoMobileApplicationsFoundException {
        TupleQueryResult result = runSparqlQuery(mobileApplicationsQueryBuilder.findAllPaginatedSimplifiedQuery(page, size));
        List<MobileApplicationBasicDataDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new NoMobileApplicationsFoundException("No Mobile Applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToMobileApplicationBasicDataDTO(result.next()));
        }
        return applicationDTOS;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> findAllApplicationsBasicData() throws NoMobileApplicationsFoundException {
        TupleQueryResult result = runSparqlQuery(mobileApplicationsQueryBuilder.findAllApplicationsBasicDataQuery());
        if (!result.hasNext()) {
            throw new NoMobileApplicationsFoundException("No Mobile Applications were found");
        }
        List<MobileApplicationBasicDataDTO> applicationDTOS = new ArrayList<>();
        while (result.hasNext()) {
            MobileApplicationFullDataDTO fullDataDTO = bindingSetToMobileApplicationFullDataDTO(result.next());
            MobileApplicationBasicDataDTO basicDataDTO = new MobileApplicationBasicDataDTO();
            basicDataDTO.setAppName(fullDataDTO.getAppName());
            basicDataDTO.setPackageName(fullDataDTO.getPackageName());
            basicDataDTO.setReviewCount(fullDataDTO.getReviewCount());
            applicationDTOS.add(basicDataDTO);
        }
        return applicationDTOS;
    }

    @Override
    public Map<String, Integer> findAllMobileApplicationFeaturesWithOccurrences(final String applicationIdentifier) {
        TupleQueryResult result = runSparqlQuery(mobileApplicationsQueryBuilder
                .findAllFeaturesWithOccurrencesAppNameQuery(applicationIdentifier));
        Map<String, Integer> featureOcurrencesDict = new HashMap<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if(bindings.getBinding("feature") != null
                    && bindings.getBinding("feature").getValue() != null
                    && bindings.getBinding("count") != null
                    && bindings.getBinding("count").getValue() != null) {
                featureOcurrencesDict.put(
                        bindings.getBinding("feature").getValue().stringValue(),
                        Integer.valueOf(bindings.getBinding("count").getValue().stringValue())
                );
            }
        }
        return featureOcurrencesDict;
    }

    @Override
    public List<String> findAllDistinctMobileApplicationFeatures(final String applicationIdentifier) {
        TupleQueryResult result = runSparqlQuery(mobileApplicationsQueryBuilder
                .findAllDistinctFeaturesByAppNameQuery(applicationIdentifier));
        List<String> featuresList = new ArrayList<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if(bindings.getBinding("feature") != null
                    && bindings.getBinding("feature").getValue() != null) {
                featuresList.add(
                        bindings.getBinding("feature").getValue().stringValue());
            }
        }
        return featuresList;
    }

    @Override
    public List<String> findAllIdentifiers() {
        TupleQueryResult result = runSparqlQuery(mobileApplicationsQueryBuilder
                .findAllMobileAppIdentifiersQuery());
        List<String> appIdentifiers = new ArrayList<>();
        while(result.hasNext()) {
            BindingSet bindings = result.next();
            if(bindings.getBinding("appIdentifier") != null
                    && bindings.getBinding("appIdentifier").getValue() != null) {
                String identifier = bindings.getBinding("appIdentifier").getValue().stringValue();
                appIdentifiers.add(identifier);
            }
        }
        return appIdentifiers;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> findAllFromMarketSegment(final String marketSegment) {
        TupleQueryResult result = runSparqlQuery(
                mobileApplicationsQueryBuilder
                        .findAllReviewsByMarketSegmentQuery(marketSegment)
        );
        List<MobileApplicationBasicDataDTO> mobileApplicationBasicDataDTOS = new ArrayList<>();
        while(result.hasNext()) {
            BindingSet bindings = result.next();
            MobileApplicationBasicDataDTO mobileApp = new MobileApplicationBasicDataDTO();
            if(bindings.getBinding("app_name") != null
                    && bindings.getBinding("app_name").getValue() != null) {
                mobileApp.setAppName(bindings.getBinding("app_name").getValue().stringValue());
            }
            if(bindings.getBinding("package_name") != null
                    && bindings.getBinding("package_name").getValue() != null) {
                mobileApp.setPackageName(bindings.getBinding("package_name").getValue().stringValue());
            }
            if(bindings.getBinding("reviewCount") != null
                    && bindings.getBinding("reviewCount").getValue() != null) {
                mobileApp.setReviewCount(Integer.valueOf(
                        bindings.getBinding("reviewCount").getValue().stringValue())
                );
            }
            if (mobileApp.getAppName() != null || mobileApp.getPackageName() != null) {
                mobileApplicationBasicDataDTOS.add(mobileApp);
            }
        }

        return mobileApplicationBasicDataDTOS;
    }
    @Override
    public MobileApplicationFullDataDTO findById(final String appName) throws MobileApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(mobileApplicationsQueryBuilder.findByNameQuery(appName));
        if (!result.hasNext()) {
            throw new MobileApplicationNotFoundException("No Mobile Application was found for name: " + appName);
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

    @Override
    public void addReviewToMobileApplication(String packageName, String reviewId) {
        List<Statement> statements = new ArrayList<>();
        IRI applicationIRI = factory.createIRI(schemaIRI.getAppIRI() + "/" + packageName);
        IRI reviewIRI = factory.createIRI(schemaIRI.getReviewIRI() + "/" + reviewId);
        statements.add(factory.createStatement(applicationIRI, schemaIRI.getHasPartIRI(), reviewIRI));
        commitChanges(statements);
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

    private void addChangelogToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                                          List<Statement> statements,
                                          IRI applicationIRI) {
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

    private void addSummaryToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                                        List<Statement> statements,
                                        IRI applicationIRI) {
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

    private void addDescriptionToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                                            List<Statement> statements,
                                            IRI applicationIRI) {
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

    private void addPackageNameToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                                            List<Statement> statements,
                                            IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getPackageName() != null) {
            statements.add(
                    factory.createStatement(
                            applicationIRI,
                            schemaIRI.getIdentifierIRI(),
                            factory.createLiteral(mobileApplicationFullDataDTO.getPackageName()
                            )
                    )
            );
        }
    }

    private void addAppNameToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                                        List<Statement> statements,
                                        IRI applicationIRI) {
        if (mobileApplicationFullDataDTO.getAppName() != null) {
            String sanitizedName = Utils.sanitizeString(mobileApplicationFullDataDTO.getAppName());
            statements.add(
                    factory.createStatement(
                            applicationIRI,
                            schemaIRI.getNameIRI(),
                            factory.createLiteral(sanitizedName)
                    )
            );
        }
    }

    private void addVersionToStatements(MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                                        List<Statement> statements,
                                        IRI applicationIRI) {
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

    private MobileApplicationFullDataDTO bindingSetToMobileApplicationFullDataDTO(final BindingSet bindings) {
        MobileApplicationFullDataDTO mobileApplicationFullDataDTO = new MobileApplicationFullDataDTO();
        if (bindings.getBinding("name") != null
                && bindings.getBinding("name").getValue() != null) {
            mobileApplicationFullDataDTO.setAppName(
                    bindings.getBinding("name").getValue().stringValue());
        }
        if (bindings.getBinding("package") != null
                && bindings.getBinding("package").getValue() != null) {
            mobileApplicationFullDataDTO.setPackageName(
                    bindings.getBinding("package").getValue().stringValue());
        }
        if (bindings.getBinding("reviewCount") != null
                && bindings.getBinding("reviewCount").getValue() != null) {
            mobileApplicationFullDataDTO.setReviewCount(
                    Integer.valueOf(bindings.getBinding("reviewCount").getValue().stringValue()));
        }
        return mobileApplicationFullDataDTO;
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
                    runSparqlQuery(reviewQueryBuilder.findReviewsByIds(reviewIds));
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
                        .add(reviewRepository.getSentenceDTO(sentencesResult));
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
