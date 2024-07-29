package upc.edu.gessi.repo.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.eclipse.rdf4j.query.algebra.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dao.ReviewSentenceAndFeatureDAO;
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
                os.close();
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(output.toString(), mapper.getTypeFactory().constructCollectionType(List.class, TermDTO.class));
            } else {
                logger.error("Python script exited with code: " + exitCode);
                logger.error("Output: " + output.toString());
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
    public List<SentenceAndFeatureDAO> executeExtractSentenceScript(final List<SentenceAndFeatureDAO> sentenceAndFeatureDAOS) {
        try {
            ClassPathResource resource = new ClassPathResource("scripts/extractSentenceFromFeature.py");
            String absoluteScriptPath = resource.getFile().getAbsolutePath();

            ProcessBuilder processBuilder = new ProcessBuilder("python", absoluteScriptPath);

            Process process = processBuilder.start();

            try (OutputStream os = process.getOutputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(os, sentenceAndFeatureDAOS);
                os.flush();
            }

            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                output.append(line);
            }

            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = stderrReader.readLine()) != null) {
                errorOutput.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper.readValue(
                        output.toString(),
                        mapper.getTypeFactory().constructCollectionType(List.class, SentenceAndFeatureDAO.class));
            } else {
                logger.error("Python script exited with code: " + exitCode);
                logger.error("Error output: " + errorOutput.toString());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("Unexpected error: " + e.toString());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<SentenceAndFeatureDAO> executeExtractSentenceFromReviewsScript(List<ReviewSentenceAndFeatureDAO> reviewSentenceAndFeatureDAOS) {
        try {
            ClassPathResource resource = new ClassPathResource("scripts/extractSentenceFromReview.py");
            String absoluteScriptPath = resource.getFile().getAbsolutePath();

            ProcessBuilder processBuilder = new ProcessBuilder("python", absoluteScriptPath);

            Process process = processBuilder.start();

            try (OutputStream os = process.getOutputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(os, reviewSentenceAndFeatureDAOS);
                os.flush();
            }

            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                output.append(line);
            }

            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = stderrReader.readLine()) != null) {
                errorOutput.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper.readValue(
                        output.toString(),
                        mapper.getTypeFactory().constructCollectionType(List.class, SentenceAndFeatureDAO.class));
            } else {
                logger.error("Python script exited with code: " + exitCode);
                logger.error("Error output: " + errorOutput.toString());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("Unexpected error: " + e.toString());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


}
