package upc.edu.gessi.repo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.domain.graph.GraphApp;
import upc.edu.gessi.repo.domain.graph.GraphNode;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppDataScannerService {

    private Logger logger = LoggerFactory.getLogger(InductiveKnowledgeService.class);

    @Value("${scanner-service.url}")
    private String url;

    public App scanApp(GraphApp app, int daysFromLastUpdate) {
        List<App> updatedApps = new ArrayList<>();
        try {
            JSONArray array = new JSONArray();
            array.put(app);
            String s = "[{\"package\":\"" + app.getIdentifier() + "\", \"name\": \"" + app.getName() + "\"}]";
            StringEntity stringEntity = new StringEntity(s);
            URI uri = new URIBuilder(url)
                    .addParameter("review_days_old", String.valueOf(daysFromLastUpdate))
                    .build();

            JSONArray response = request(uri, stringEntity);

            updatedApps = new ObjectMapper().readValue(response.toString(), new TypeReference<List<App>>() {});

        } catch (UnsupportedEncodingException | JsonProcessingException | URISyntaxException e) {
            e.printStackTrace();
        }
        return updatedApps.get(0);
    }

    private JSONArray request(URI uri, StringEntity entity) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(uri);
            request.addHeader("Content-Type", "application/json");

            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("Error occurred. HTTP Status Code: {}", statusCode);
                return null; // Or throw an exception if appropriate
            }

            String jsonResponse = EntityUtils.toString(response.getEntity());
            return new JSONArray(jsonResponse);

        } catch (Exception ex) {
            logger.error("Error occurred", ex);
            return null; // Or throw an exception if appropriate
        }
    }

}
