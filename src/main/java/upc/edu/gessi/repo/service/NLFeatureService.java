package upc.edu.gessi.repo.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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
import upc.edu.gessi.repo.domain.AnalyzedDocument;
import upc.edu.gessi.repo.domain.Document;
import upc.edu.gessi.repo.utils.Utils;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class NLFeatureService {

    private Logger logger = LoggerFactory.getLogger(NLFeatureService.class);

    @Value("${nl-feature-extraction.url}")
    private String nlFeatureExtractionEndpoint;

    public List<AnalyzedDocument> getNLFeatures(List<AnalyzedDocument> documents) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        List<AnalyzedDocument> analyzedDocuments = new ArrayList<>();
        try {
            HttpPost request = new HttpPost(nlFeatureExtractionEndpoint);
            request.addHeader("Content-Type", "application/json");

            JSONArray array = new JSONArray();
            for (AnalyzedDocument doc : documents) {
                doc.setText(Utils.escape(doc.getText()));
                JSONObject obj = new JSONObject();
                obj.put("id", doc.getId());
                obj.put("text", doc.getText());
                array.put(obj);
            }

            JSONObject object = new JSONObject();
            object.put("text", array);
            object.put("ignore-verbs", new JSONArray());
            request.setEntity(new StringEntity(object.toString()));
            HttpResponse response = httpClient.execute(request);

            JSONArray responseBody = new JSONArray(EntityUtils.toString(response.getEntity()));

            for (int i = 0; i < responseBody.length(); ++i) {
                JSONObject document = responseBody.getJSONObject(i);

                JSONArray featureJSONArray = document.getJSONArray("features");
                List<String> features = new ArrayList<>();
                for (int j = 0; j < featureJSONArray.length(); ++j) {
                    features.add(featureJSONArray.getString(j));
                }

                AnalyzedDocument analyzedDoc =
                        new AnalyzedDocument(document.getString("id"), features);
                analyzedDocuments.add(analyzedDoc);
            }

        } catch (Exception ex) {
            logger.error("There was some error with feature extraction");
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return analyzedDocuments;
        }
    }

}
