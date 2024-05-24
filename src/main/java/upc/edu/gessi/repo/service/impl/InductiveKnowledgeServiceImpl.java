package upc.edu.gessi.repo.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.graph.GraphEdge;
import upc.edu.gessi.repo.dto.graph.GraphNode;
import upc.edu.gessi.repo.service.InductiveKnowledgeService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
@Lazy
public class InductiveKnowledgeServiceImpl implements InductiveKnowledgeService {

    private Logger logger = LoggerFactory.getLogger(InductiveKnowledgeServiceImpl.class);

    @Value("${inductive-knowledge-service.url}")
    private String url;
    private void request(StringEntity entity) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(url);
            request.addHeader("Content-Type", "application/json");

            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.error("There was some error");
            }

        } catch (Exception ex) {
            logger.error("There was some error with feature extraction");
        }
    }


    public void addNodes(List<GraphNode> nodeList) {
        try {
            JSONArray array = new JSONArray();
            for (GraphNode node : nodeList) array.put(node);
            StringEntity stringEntity = new StringEntity(array.toString());
            request(stringEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addEdges(List<GraphEdge> edgeList) {
        try {
            JSONArray array = new JSONArray();
            for (GraphEdge edge : edgeList) array.put(edge);
            StringEntity stringEntity = new StringEntity(array.toString());
            request(stringEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<String> computeHierarchicalClustering() {
        // Step 1 - Obtain all features
        String[] features = {"video call", "secure video call", "secure efficient video call", "efficient video call", "random video call", "random secure video call"};
        // Step 2 - Make a list and remove repeated ones

        // Step 3 - Make a matrix N x N with all features
        Integer columns = features.length;
        Integer rows = columns;

        Integer[][] distanceMatrix = new Integer[rows][columns];

        // Step 4 - Compute all cells distances using  Levenshtein distance
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                }
            }
        }
        // Step 5 - Do complete linkage clustering algorithm
        // Step 6 - Format output
        return new ArrayList<>();
    }
}
