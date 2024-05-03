package upc.edu.gessi.repo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.MobileApplicationDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.service.AppDataScannerService;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppDataScannerServiceImpl implements AppDataScannerService {

    private Logger logger = LoggerFactory.getLogger(InductiveKnowledgeServiceImpl.class);

    @Value("${scanner-service.url}")
    private String url;
    private HttpResponse request(URI uri, StringEntity entity) {
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

            return response;
            //String jsonResponse = EntityUtils.toString(response.getEntity());
            //return new JSONArray(jsonResponse);

        } catch (Exception ex) {
            logger.error("Error occurred", ex);
            return null; // Or throw an exception if appropriate
        }
    }

    public MobileApplicationDTO scanApp(GraphApp app, int daysFromLastUpdate) {
        List<MobileApplicationDTO> updatedCompleteApplicationDataDTOS = new ArrayList<>();
        try {
            JSONArray array = new JSONArray();
            array.put(app);
            String s = "[{\"package\":\"" + app.getIdentifier() + "\", \"name\": \"" + app.getName() + "\"}]";
            StringEntity stringEntity = new StringEntity(s);
            URI uri = new URIBuilder(url)
                    .addParameter("review_days_old", String.valueOf(daysFromLastUpdate))
                    .addParameter("return_data", "true")
                    .addParameter("web-scrapers", "false")
                    .build();

            //JSONArray response = request(uri, stringEntity);

            try {
                InputStream inputStream = request(uri, stringEntity).getEntity().getContent();
                updatedCompleteApplicationDataDTOS = new ObjectMapper().readValue(inputStream, new TypeReference<List<MobileApplicationDTO>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException | URISyntaxException e) {
            e.printStackTrace();
        }
        return updatedCompleteApplicationDataDTOS.size() > 0 ? updatedCompleteApplicationDataDTOS.get(0) : null;
    }


}
