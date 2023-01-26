package upc.edu.gessi.repo.utils;

import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.service.GraphDBService;

import java.io.IOException;

public class NLExtractionThread implements Runnable {

    private Logger logger = LoggerFactory.getLogger(NLExtractionThread.class);

    private final GraphDBService dbConnection;

    private final Notifier notifier;

    private Integer batchSize;
    private Integer from;
    private Double subjectivityThreshold;
    private Repository repository;
    private int currentRequestId;
    private DocumentType documentType = DocumentType.REVIEWS;

    public NLExtractionThread(GraphDBService dbConnection,
                                Notifier notifier) {
        this.dbConnection = dbConnection;
        this.notifier = notifier;
    }

    public int setParameters(Integer batchSize, Integer from, Double subjectivityThreshold) {
        this.batchSize = batchSize;
        this.from = from;
        this.subjectivityThreshold = subjectivityThreshold;
        Long currentTime = System.currentTimeMillis();
        this.currentRequestId = currentTime.hashCode();
        return this.currentRequestId;
    }

    public int setParameters(Integer batchSize, Integer from, DocumentType type) {
        this.batchSize = batchSize;
        this.from = from;
        this.documentType = type;
        Long currentTime = System.currentTimeMillis();
        this.currentRequestId = currentTime.hashCode();
        return this.currentRequestId;
    }

    @Override
    public void run() {
        NLRequest nlRequest = null;
        try {
            int res = -1;
            if (documentType == DocumentType.REVIEWS) {
                res =  dbConnection.extractFeaturesFromReviews(batchSize, from, subjectivityThreshold);
            }
            else if (documentType == DocumentType.ALL) {
                logger.info("Deducting features from descriptions...");
                dbConnection.extractFeaturesByDocument(DocumentType.DESCRIPTION, batchSize);
                logger.info("Deducting features from changelogs...");
                dbConnection.extractFeaturesByDocument(DocumentType.CHANGELOG, batchSize);
                logger.info("Deducting features from summaries...");
                dbConnection.extractFeaturesByDocument(DocumentType.SUMMARY, batchSize);
            }
            else {
                dbConnection.extractFeaturesByDocument(documentType, batchSize);
            }

            nlRequest = new NLRequest(this.currentRequestId, "OK", res);

        }
        catch (Exception e) {
            nlRequest = new NLRequest(this.currentRequestId, "ERROR", dbConnection.getCount());
        }
        finally {
            try {
                this.notifier.notifyObservers(nlRequest);
            }
            catch (IOException ioex) {
                logger.info("InputOutput exception");
            }
        }
    }
}
