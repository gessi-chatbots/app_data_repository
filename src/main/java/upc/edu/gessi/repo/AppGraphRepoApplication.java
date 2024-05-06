package upc.edu.gessi.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class AppGraphRepoApplication {

	private Logger logger = LoggerFactory.getLogger(AppGraphRepoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AppGraphRepoApplication.class, args);
	}

}
