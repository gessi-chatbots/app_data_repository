package upc.edu.gessi.repo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import upc.edu.gessi.repo.domain.App;
import upc.edu.gessi.repo.domain.Document;
import upc.edu.gessi.repo.domain.DocumentType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class NLFeatureService {

    @Value("${nl-feature-extraction.url}")
    private String nlFeatureExtractionEndpoint;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    public List<String> getNLFeatures(String text) {
        List<String> features = new ArrayList<>();
        //System.out.println(text);
        //TODO
        /** return nlFeatureService
                .post()
                .uri("/extract-features")
                .bodyValue(document)
                .retrieve()
                .bodyToMono(String[].class)
                .block(REQUEST_TIMEOUT); */
        features.add("test-feature");
        return features;
    }


}
