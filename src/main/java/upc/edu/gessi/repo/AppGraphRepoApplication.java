package upc.edu.gessi.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.service.AppFinder;
import upc.edu.gessi.repo.service.GraphDBService;
import upc.edu.gessi.repo.service.NLFeatureService;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@RestController
public class AppGraphRepoApplication {

	@Autowired
	private GraphDBService dbConnection;

	@Autowired
	private AppFinder appFinder;

	private Logger logger = LoggerFactory.getLogger(AppGraphRepoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AppGraphRepoApplication.class, args);
	}

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

	/**
	 * DEDUCTIVE KNOWLEDGE - App Features from Natural Language documents
	 * @param documentType
	 */

	@PostMapping("/derivedNLFeatures")
	public void derivedNLFeatures(@RequestParam("documentType") DocumentType documentType) {
		logger.info("Generating derived deductive knowledge from natural language documents");
		logger.info("Document type: " + documentType);
		dbConnection.extractFeaturesByDocument(documentType);
	}

}
