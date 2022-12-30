package upc.edu.gessi.repo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import upc.edu.gessi.repo.service.GraphDBService;

import java.io.IOException;

public class NLExtractionThread implements Runnable {

    private Logger logger = LoggerFactory.getLogger(NLExtractionThread.class);

    private final GraphDBService dbConnection;

    private final Notifier notifier;

    private Integer batchSize;
    private Integer from;
    private Double subjectivityThreshold;

    private int currentRequestId;

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

    @Override
    public void run() {
        NLRequest nlRequest = null;
        try {
            int res = dbConnection.extractFeaturesFromReviews(batchSize, from, subjectivityThreshold);
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
