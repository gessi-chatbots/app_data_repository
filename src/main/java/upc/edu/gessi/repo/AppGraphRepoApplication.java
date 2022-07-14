package upc.edu.gessi.repo;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
		App app = null;
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
	public int insertData(@RequestBody String text) throws ClassNotFoundException {
		App app = new Gson().fromJson(text, App.class);
		dbConnection.insertApp(app,app.getName());
		return 1;
	}


}
