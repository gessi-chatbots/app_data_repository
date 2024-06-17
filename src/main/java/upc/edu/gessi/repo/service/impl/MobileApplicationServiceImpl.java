package upc.edu.gessi.repo.service.impl;



import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SchemaDO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.MobileApplications.NoMobileApplicationsFoundException;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.MobileApplicationRepository;
import upc.edu.gessi.repo.repository.RepositoryFactory;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.service.AppDataScannerService;
import upc.edu.gessi.repo.service.MobileApplicationService;
import upc.edu.gessi.repo.service.ReviewService;
import upc.edu.gessi.repo.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
public class MobileApplicationServiceImpl implements MobileApplicationService {

    private final RepositoryFactory repositoryFactory;

    private final AppDataScannerService appDataScannerService;

    private final ReviewService reviewService;

    private final Logger logger = LoggerFactory.getLogger(MobileApplicationService.class);

    @Autowired
    public MobileApplicationServiceImpl(final RepositoryFactory repoFact,
                                        final AppDataScannerService appDataScannerSv,
                                        final ReviewService reviewSv) {
        repositoryFactory = repoFact;
        appDataScannerService = appDataScannerSv;
        reviewService = reviewSv;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> getAllBasicData() throws NoMobileApplicationsFoundException {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAllApplicationsBasicData();
    }

    @Override
    public List<GraphApp> getAllApps() {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).getAllApps();
    }

    @Override
    public void addFeatures(final MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                            final IRI sub,
                            final List<Statement> statements) {
        ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).addFeature(mobileApplicationFullDataDTO, sub, statements);
    }

    @Override
    public List<MobileApplicationFullDataDTO> create(List<MobileApplicationFullDataDTO> dtos) {
        for (MobileApplicationFullDataDTO mobileApplicationFullDataDTO : dtos) {
            insertMobileApplication(mobileApplicationFullDataDTO);
            insertMobileApplicationReviews(mobileApplicationFullDataDTO);
        }
        return dtos;
    }


    @Override
    public MobileApplicationFullDataDTO get(String id) throws ObjectNotFoundException {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findById(Utils.sanitizeString(id));
    }

    @Override
    public List<MobileApplicationFullDataDTO> getListed(List<String> ids) throws NoObjectFoundException {
        List<MobileApplicationFullDataDTO> mobileApplicationFullDataDTOS = new ArrayList<>();
        for (String id : ids) {
            try {
                mobileApplicationFullDataDTOS.add(((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findById(Utils.sanitizeString(id)));
            } catch (ObjectNotFoundException ignored) {
            }
        }
        return mobileApplicationFullDataDTOS;
    }

    @Override
    public List<MobileApplicationFullDataDTO> getAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        try {
            return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAllPaginated(page, size);
        } catch (NoMobileApplicationsFoundException noMobileApplicationsFoundException) {
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MobileApplicationBasicDataDTO> getAllBasicDataPaginated(Integer page, Integer size) throws NoMobileApplicationsFoundException{
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAllBasicDataPaginated(page, size);
    }
    @Override
    public List<MobileApplicationFullDataDTO> getAll() {
        try {
            return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAll();
        } catch (NoMobileApplicationsFoundException noMobileApplicationsFoundException) {
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void update(MobileApplicationFullDataDTO entity) {
        ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).update(entity);
    }

    @Override
    public void updateOld(int daysFromLastUpdate) {
        updateApp(daysFromLastUpdate);
    }

    @Override
    public byte[] getAllFromMarketSegment(final Integer size, final String marketSegment) {
        List<MobileApplicationBasicDataDTO> apps =
                ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class))
                        .findAllFromMarketSegment(marketSegment);
        logger.info("Extracted {} applications from Market Segment {}", apps.size(), marketSegment);

        Integer reviewsToExtractPerApp = size/apps.size();
        logger.info("Proceeding to extract {} reviews per mobile application", reviewsToExtractPerApp);

        List<ReviewDTO> extractedReviews = new ArrayList<>();

        for(MobileApplicationBasicDataDTO app : apps) {
            try {
                List<ReviewDTO> rev = ((ReviewRepository) useRepository(ReviewRepository.class))
                        .getReviewsByAppNameAndIdentifierWithLimit(app.getAppName(),
                                app.getPackageName(),
                                reviewsToExtractPerApp);
                logger.info("Extracted {} reviews from Mobile Application {}", rev.size(), app.getAppName());
                extractedReviews.addAll(rev);
            } catch (Exception e) {
                logger.error("There was an error extracting the reviews from application {}", app.getAppName());
            }
        }


        logger.info("Proceeding to write in file...");
        org.apache.jena.rdf.model.Model model
                = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
        for (ReviewDTO review : extractedReviews) {
            org.apache.jena.rdf.model.Resource reviewResource = model.createResource("https://schema.org/Review/" + review.getId())
                    .addProperty(RDF.type, SchemaDO.Review)
                    .addProperty(SchemaDO.identifier, review.getId())
                    .addProperty(SchemaDO.author, review.getAuthor())
                    .addProperty(SchemaDO.datePublished, review.getDate().toString())
                    .addProperty(SchemaDO.reviewBody, review.getReviewText());
        }

        logger.info("Generating file...");
        String pathname = marketSegment + size + "ExtractedReviews.ttl";
        File ttlFile = new File("src/main/resources/static/ttl/" + pathname);
        try (FileWriter out = new FileWriter(ttlFile)) {
            model.write(out, "TURTLE");
        } catch (Exception e) {
            logger.error("Error writing to TTL file", e);
        }

        byte[] fileContent = null;
        try {
            fileContent = Files.readAllBytes(ttlFile.toPath());
        } catch (Exception e) {
            logger.error("Error reading TTL file into byte array", e);
        }
        logger.info("Done");
        return fileContent;
    }

    @Override
    public void delete(String id) {
        ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).delete(id);
    }

    private void insertMobileApplication(MobileApplicationFullDataDTO mobileApplicationFullDataDTO) {
        ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).insert(mobileApplicationFullDataDTO);
    }

    private void insertMobileApplicationReviews(MobileApplicationFullDataDTO mobileApplicationFullDataDTO) {
        for (ReviewDTO reviewDTO : mobileApplicationFullDataDTO.getReviews()) {
            ((ReviewRepository) useRepository(ReviewRepository.class)).insert(reviewDTO);
            ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class))
                    .addReviewToMobileApplication(
                            mobileApplicationFullDataDTO.getPackageName(),
                            reviewDTO.getId()
                    );
        }
    }
    private void updateApp(int daysFromLastUpdate) {
        List<GraphApp> apps = getAllApps();
        for (GraphApp app : apps) {

            MobileApplicationFullDataDTO updatedCompleteApplicationDataDTO = appDataScannerService.scanApp(app, daysFromLastUpdate);

            if (updatedCompleteApplicationDataDTO != null) {
                ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).insert(updatedCompleteApplicationDataDTO);
            }
        }
    }
    private Object useRepository(Class<?> clazz) {
        return repositoryFactory.createRepository(clazz);
    }


    /*
        public List<String> getResultsContaining(String text) {
            RepositoryConnection repoConnection = repository.getConnection();
            String query = "PREFIX gessi: <http://gessi.upc.edu/app/> SELECT ?x ?y ?z " +
                                                                        "WHERE {?x ?y ?z .FILTER regex(str(?z), \""+text+"\")}" ;
            TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
            List<String> resultList = new ArrayList<>();
            TupleQueryResult result = tupleQuery.evaluate();
            while (result.hasNext()) {  // iterate over the result
                BindingSet bindingSet = result.next();
                Value valueOfX = bindingSet.getValue("x");
                Value valueOfY = bindingSet.getValue("y");
                Value valueOfZ = bindingSet.getValue("z");
                resultList.add(valueOfZ.stringValue());
            }
            return resultList;
        }
    */
}
