package upc.edu.gessi.repo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.domain.App;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@RestController
public class AppGraphRepoApplication {

	private static AppFinder appFinder;
	private static DBConnection dbConnection;



	public static void main(String[] args) {
		try {
			String url = new InitConfig().getServerURL();
			appFinder = new AppFinder(url);
			dbConnection = new DBConnection(url);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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


}
