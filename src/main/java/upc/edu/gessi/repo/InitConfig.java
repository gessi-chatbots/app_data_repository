package upc.edu.gessi.repo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class InitConfig {
    InputStream inputStream;

    public String getServerURL() throws IOException {
        Properties prop = new Properties();
        String propFileName = "config.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        prop.load(inputStream);
        return prop.getProperty("db_url");
    }
}
