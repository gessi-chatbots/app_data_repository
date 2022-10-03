package upc.edu.gessi.repo.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class NLFeatureService {

    private Logger logger = LoggerFactory.getLogger(NLFeatureService.class);

    @Value("${nl-feature-extraction.url}")
    private String nlFeatureExtractionEndpoint;

    public List<String> getNLFeatures(String text) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        List<String> features = new ArrayList<>();
        try {
            HttpPost request = new HttpPost(nlFeatureExtractionEndpoint);
            request.addHeader("Content-Type", "application/json");

            JSONArray array = new JSONArray();
            array.put(escape(text));
            request.setEntity(new StringEntity(array.toString()));
            HttpResponse response = httpClient.execute(request);

            JSONArray responseBody = new JSONArray(EntityUtils.toString(response.getEntity()));

            for (int i = 0; i < responseBody.length(); ++i) {
                features.add(responseBody.getString(i));
            }

        } catch (Exception ex) {
            logger.error("There was some error with feature extraction");
        } finally {
            return features;
        }
    }

    private String escape(String text) {
        return text.replaceAll("[^ -~]+", "");
    }

}
