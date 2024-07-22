package upc.edu.gessi.repo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dao.SentenceAndFeatureDAO;
import upc.edu.gessi.repo.dto.TermDTO;
import upc.edu.gessi.repo.service.ProcessService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Lazy
public class ProcessServiceImpl implements ProcessService {
    private final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);

    private String executePythonScript(final String scriptPath,
                                       final Object inputData) throws Exception {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        String absoluteScriptPath = resource.getFile().getAbsolutePath();

        ProcessBuilder processBuilder = new ProcessBuilder("python", absoluteScriptPath);
        Process process = processBuilder.start();

        try (OutputStream os = process.getOutputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(os, inputData);
            os.flush();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            return output.toString();
        } else {
            logger.error("Python script exited with code: " + exitCode);
            throw new RuntimeException("Python script exited with code: " + exitCode);
        }
    }

    @Override
    public List<TermDTO> executeTop50PythonScript(String scriptPath, List<SentenceAndFeatureDAO> distinctFeatures) {
        try {
            String output = executePythonScript(scriptPath, distinctFeatures);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(
                    output,
                    mapper.getTypeFactory().constructCollectionType(List.class, TermDTO.class));
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return new ArrayList<>();
        }
    }

    @Override
    public String executeHeatMapPythonScript(final List<String> distinctFeatures,
                                             final List<TermDTO> verbs,
                                             final List<TermDTO> nouns) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            ObjectNode verbsNode = rootNode.putObject("verbs");
            for (TermDTO verb : verbs) {
                verbsNode.put(verb.getTerm(), verb.getFrequency());
            }

            ObjectNode nounsNode = rootNode.putObject("nouns");
            for (TermDTO noun : nouns) {
                nounsNode.put(noun.getTerm(), noun.getFrequency());
            }

            executePythonScript("scripts/noun_verb_heat_matrix_generator.py", rootNode.toString());
            Path csvFilePath = Paths.get("verb_noun_heat_matrix.csv");

            return new String(Files.readAllBytes(csvFilePath));
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return null;
        }
    }

    @Override
    public String executeExtractSentenceScript(final String sentence, final String feature) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();

            rootNode.put("sentence", sentence);
            rootNode.put("feature", feature);

            String jsonOutput = executePythonScript("scripts/extractSentenceFromFeature.py", rootNode.toString());

            JsonNode outputNode = mapper.readTree(jsonOutput);
            return outputNode.get("extracted_sentence").asText();
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return null;
        }
    }
}
