package upc.edu.gessi.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.domain.SimilarityAlgorithm;
import upc.edu.gessi.repo.domain.SimilarityApp;
import upc.edu.gessi.repo.service.AppFinder;
import upc.edu.gessi.repo.service.GraphDBService;
import upc.edu.gessi.repo.service.NLFeatureService;
import upc.edu.gessi.repo.service.SimilarityService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class AppGraphRepoApplication {

	@Autowired
	private GraphDBService dbConnection;

	@Autowired
	private SimilarityService similarityService;

	@Autowired
	private AppFinder appFinder;

	private Logger logger = LoggerFactory.getLogger(AppGraphRepoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AppGraphRepoApplication.class, args);
	}

	/**
	 * CRUD OPERATIONS
	 */
	@GetMapping("/data")
	public App getData(@RequestParam(value = "app_name", defaultValue = "OsmAnd") String name) {
		App app;
		try {
			app =  appFinder.retrieveAppByName(name.toLowerCase());
			return app;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
			//throw new RuntimeException(e);
		}
	}
	@PostMapping("/insert")
	public int insertData(@RequestBody List<App> apps) {
		for (App app : apps) {
			dbConnection.insertApp(app);
		}
		return 1;
	}

	@GetMapping("/export")
	public void export(@RequestParam(value = "fileName") String fileName) throws Exception{
		logger.info("Initializing export...");
		dbConnection.exportRepository(fileName);
		logger.info("Repository successfully exported at " + fileName);
	}

	/**
	 * DEDUCTIVE KNOWLEDGE - App Features from Natural Language documents
	 */

	@GetMapping("/getLastReview")
	public int getLastReview() {
		return dbConnection.getCount();
	}

	@PostMapping("/derivedNLFeatures")
	public int derivedNLFeatures(@RequestParam(value = "documentType") DocumentType documentType,
								  @RequestParam(value = "batch-size") Integer batchSize,
								  @RequestParam(value = "from") Integer from) {
		logger.info("Generating derived deductive knowledge from natural language documents");
		logger.info("Document type: " + documentType);
		if (documentType.equals(DocumentType.REVIEWS)) {
			logger.info("Deducting features from reviews...");
			try {
				return dbConnection.extractFeaturesFromReviews(batchSize, from);
			} catch (Exception e) {
				return dbConnection.getCount();
			}
		} else if (!documentType.equals(DocumentType.ALL)) {
			logger.info("Deducting features from " + documentType.getName());
			dbConnection.extractFeaturesByDocument(documentType,batchSize);
		} else {
			logger.info("Deducting features from descriptions...");
			dbConnection.extractFeaturesByDocument(DocumentType.DESCRIPTION,batchSize);
			logger.info("Deducting features from changelogs...");
			dbConnection.extractFeaturesByDocument(DocumentType.CHANGELOG,batchSize);
			logger.info("Deducting features from summaries...");
			dbConnection.extractFeaturesByDocument(DocumentType.SUMMARY,batchSize);
			//logger.info("Deducting features from reviews...");
			//dbConnection.extractFeaturesFromReviews();
		}
		return -1;
	}

	/**
	 * DEDUCTIVE KNOWLEGDE - Similarity between features (materialized in graph)
	 */
	@PostMapping("computeFeatureSimilarity")
	public void computeFeatureSimilarity(@RequestParam(defaultValue = "0.5", name = "threshold") double synonymThreshold) {
		similarityService.computeFeatureSimilarity(synonymThreshold);
	}

	@DeleteMapping("deleteFeatureSimilarities")
	public void deleteFeatureSimilarities() {
		similarityService.deleteFeatureSimilarities();
	}

	/**
	 * INDUCTIVE KNOWLEDGE - Extract/report summary from inductive knowledge
	 */

	@PostMapping("computeSimilarity")
	public void computeSimilarity(@RequestParam(defaultValue = "JACCARD") SimilarityAlgorithm algorithm) {
		similarityService.computeSimilarity(algorithm);
	}

	@GetMapping("findSimilarApps")
	public Map<String, List<SimilarityApp>> getTopKSimilarApps(@RequestBody List<String> apps,
															   @RequestParam Integer k,
															   @RequestParam DocumentType documentType) {
		return similarityService.getTopKSimilarApps(apps, k, documentType);
	}

	@GetMapping("findAppsByFeature")
	public Map<String, List<SimilarityApp>> findAppsByFeature(@RequestBody List<String> features,
															   @RequestParam Integer k,
															   @RequestParam DocumentType documentType) {
		return similarityService.findAppsByFeature(features, k, documentType);
	}

}
