package upc.edu.gessi.repo.service.impl;

import jakarta.json.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import upc.edu.gessi.repo.dto.AnalyzedDocumentDTO;
import upc.edu.gessi.repo.dto.Review.HUBResponseDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.service.NLFeatureService;
import upc.edu.gessi.repo.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
public class NLFeatureServiceImpl implements NLFeatureService {

    private Logger logger = LoggerFactory.getLogger(NLFeatureServiceImpl.class);

    private final RestTemplate restTemplate;

    @Autowired
    public NLFeatureServiceImpl(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Value("${transfeatex.url}")
    private String nlFeatureExtractionEndpoint;

    @Value("${hub.url}")
    private String hubFeatureAnalysisEndpoint;

    public List<AnalyzedDocumentDTO> getNLFeatures(List<AnalyzedDocumentDTO> documents) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        List<AnalyzedDocumentDTO> analyzedDocumentDTOS = new ArrayList<>();
        try {
            HttpPost request = new HttpPost(nlFeatureExtractionEndpoint);
            request.addHeader("Content-Type", "application/json");

            JSONArray array = new JSONArray();
            for (AnalyzedDocumentDTO doc : documents) {
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

                AnalyzedDocumentDTO analyzedDoc =
                        new AnalyzedDocumentDTO(document.getString("id"), features);
                analyzedDocumentDTOS.add(analyzedDoc);
            }

        } catch (Exception ex) {
            logger.error("There was some error with feature extraction");
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return analyzedDocumentDTOS;
        }
    }

    public List<ReviewDTO> getHUBFeatures(List<ReviewDTO> reviews, String featureModel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<ReviewDTO>> requestBody = new HttpEntity<>(reviews, headers);

        String url = hubFeatureAnalysisEndpoint + "?feature_model=" + featureModel;

        HUBResponseDTO responseDTO = restTemplate.postForObject(
                url, requestBody, HUBResponseDTO.class);

        return responseDTO.getAnalyzed_reviews();
    }

}
