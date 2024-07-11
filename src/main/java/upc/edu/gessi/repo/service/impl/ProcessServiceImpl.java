package upc.edu.gessi.repo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.TermDTO;
import upc.edu.gessi.repo.service.ProcessService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
public class ProcessServiceImpl implements ProcessService {
    private final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);


    @Override
    public List<TermDTO> executeTop50PythonScript(String scriptPath, List<String> distinctFeatures) {
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
                List<TermDTO> termDTOList = mapper
                        .readValue(
                                output.toString(),
                                mapper.getTypeFactory().constructCollectionType(List.class, TermDTO.class));
                return termDTOList;
            } else {
                logger.error("Python script exited with code: " + exitCode);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
