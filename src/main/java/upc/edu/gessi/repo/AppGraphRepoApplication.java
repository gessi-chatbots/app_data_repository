package upc.edu.gessi.repo;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class AppGraphRepoApplication {

	private final AppFinder appFinder = new AppFinder("http://agustiubu:7200/repositories/app-data-repo");
	private final DBConnection dbConnection = new DBConnection("http://agustiubu:7200/repositories/app-data-repo");

	public static void main(String[] args) {
		SpringApplication.run(AppGraphRepoApplication.class, args);
	}

	@GetMapping("/data")
	public App getData(@RequestParam(value = "app_name", defaultValue = "OsmAnd") String name) {
		App app = null;
		try {
			app =  appFinder.retrieveInfo(name.toLowerCase());
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
	public int insertData(@RequestBody String text) throws ClassNotFoundException {
		App app = new Gson().fromJson(text, App.class);
		//dbConnection.insertData("http://gessi.upc.edu/app/OsmAnd","http://schema.org/name", app.getName());
		dbConnection.insertApp(app,app.getName());
		return 1;
	}


}
