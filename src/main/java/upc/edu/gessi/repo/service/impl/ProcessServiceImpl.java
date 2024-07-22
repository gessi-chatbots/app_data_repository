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
import java.io.InputStream;
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


    @Override
    public List<TermDTO> executeTop50PythonScript(String scriptPath, List<SentenceAndFeatureDAO> distinctFeatures) {
        try {
            ClassPathResource resource = new ClassPathResource(scriptPath);
            String absoluteScriptPath = resource.getFile().getAbsolutePath();

            ProcessBuilder processBuilder = new ProcessBuilder("python", absoluteScriptPath);

            Process process = processBuilder.start();

            try (OutputStream os = process.getOutputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(os, distinctFeatures);
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
                ObjectMapper mapper = new ObjectMapper();
                return mapper
                        .readValue(
                                output.toString(),
                                mapper.getTypeFactory().constructCollectionType(List.class, TermDTO.class));
            } else {
                logger.error("Python script exited with code: " + exitCode);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("Unexpected error: ", e.toString());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public String executeHeatMapPythonScript(final List<String> distinctFeatures,
                                           final List<TermDTO> verbs,
                                           final List<TermDTO> nouns) {
        try {
            ClassPathResource resource = new ClassPathResource("scripts/noun_verb_heat_matrix_generator.py");
            String absoluteScriptPath = resource.getFile().getAbsolutePath();

            ProcessBuilder processBuilder = new ProcessBuilder("python", absoluteScriptPath);
            Process process = processBuilder.start();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            /*
            rootNode.putArray("distinct_features").addAll(
                    distinctFeatures.stream().map(TextNode::new).collect(Collectors.toList())
            );*/

            ObjectNode verbsNode = rootNode.putObject("verbs");
            for (TermDTO verb : verbs) {
                verbsNode.put(verb.getTerm(), verb.getFrequency());
            }

            ObjectNode nounsNode = rootNode.putObject("nouns");
            for (TermDTO noun : nouns) {
                nounsNode.put(noun.getTerm(), noun.getFrequency());
            }

            try (OutputStream os = process.getOutputStream()) {
                mapper.writeValue(os, rootNode.toString());
                os.flush();
            }

            process.waitFor();
            Path csvFilePath = Paths.get("verb_noun_heat_matrix.csv");

            return new String(Files.readAllBytes(csvFilePath));

        } catch (Exception e) {
            logger.error("Unexpected error: ", e.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String executeExtractSentenceScript(String sentence, String feature) {
        try {
            ClassPathResource resource = new ClassPathResource("scripts/extractSentenceFromFeature.py");
            String absoluteScriptPath = resource.getFile().getAbsolutePath();

            ProcessBuilder processBuilder = new ProcessBuilder("python", absoluteScriptPath);
            processBuilder.redirectErrorStream(true);  // Redirect stderr to stdout
            Process process = processBuilder.start();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("sentence", sentence);
            rootNode.put("feature", feature);

            try (OutputStream os = process.getOutputStream()) {
                mapper.writeValue(os, rootNode);
                os.flush();
            }

            try (InputStream is = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String output = reader.lines().collect(Collectors.joining("\n"));
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    logger.error("Python script exited with code: " + exitCode);
                    logger.error("Error output: " + output);
                    throw new RuntimeException("Python script exited with code: " + exitCode);
                }

                return output;
            }
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            e.printStackTrace();
            return null;
        }
    }
}
