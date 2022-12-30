package upc.edu.gessi.repo.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Notifier {

    @Value("${registry.url}")
    private String registryUrl;

    public void notifyObservers(NLRequest nlRequest) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(registryUrl);
        request.addHeader("Content-Type", "application/json");

        JSONObject obj = new JSONObject();
        if (nlRequest != null) {
            obj.put("requestId", nlRequest.requestId());
            obj.put("status", nlRequest.status());
            obj.put("reviewCount", nlRequest.reviewCount());
        }
        request.setEntity(new StringEntity(obj.toString()));
        httpClient.execute(request);
    }
}
