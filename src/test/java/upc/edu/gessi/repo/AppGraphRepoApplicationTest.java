package upc.edu.gessi.repo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import upc.edu.gessi.repo.domain.DocumentType;
import upc.edu.gessi.repo.service.GraphDBService;
import upc.edu.gessi.repo.service.SimilarityService;
import upc.edu.gessi.repo.utils.NLExtractionThread;
import upc.edu.gessi.repo.utils.Notifier;

import java.util.HashMap;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@AutoConfigureJsonTesters
@SpringBootTest
@AutoConfigureMockMvc
class AppGraphRepoApplicationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GraphDBService dbConnection;

    @MockBean
    private SimilarityService similarityService;


    @MockBean
    private Notifier notifier;

    @Test
    void derivedNLFeaturesReviewsOK() throws Exception {

        given(dbConnection.startFeaturesFromReviewsExtraction(100,0,0.2)).willReturn(123);

        MockHttpServletResponse response = mvc.perform(
                post("/derivedNLFeatures?documentType=REVIEWS&batch-size=100&maxSubj=0.2").
                        contentType(MediaType.APPLICATION_JSON).content(
                                ""
                )).andReturn().getResponse();

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(Integer.toString(123), response.getContentAsString());

    }

    @Test
    void derivedNLFeaturesAllOK() throws Exception {

        given(dbConnection.startFeaturesByDocumentExtraction(DocumentType.ALL,100)).willReturn(123);

        MockHttpServletResponse response = mvc.perform(
                post("/derivedNLFeatures?documentType=ALL&batch-size=100").
                        contentType(MediaType.APPLICATION_JSON).content(
                                ""
                        )).andReturn().getResponse();

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(Integer.toString(123), response.getContentAsString());

    }

    @Test
    void derivedNLFeaturesSpecificType() throws Exception {

        given(dbConnection.startFeaturesByDocumentExtraction(DocumentType.DESCRIPTION,100)).willReturn(123);

        MockHttpServletResponse response = mvc.perform(
                post("/derivedNLFeatures?documentType=DESCRIPTION&batch-size=100").
                        contentType(MediaType.APPLICATION_JSON).content(
                                ""
                        )).andReturn().getResponse();

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(Integer.toString(123), response.getContentAsString());

    }

}