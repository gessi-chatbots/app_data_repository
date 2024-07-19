package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.dao.SentenceAndFeatureDAO;
import upc.edu.gessi.repo.dto.TermDTO;

import java.util.List;

public interface ProcessService {
    List<TermDTO> executeTop50PythonScript(final String scriptPath, final List<SentenceAndFeatureDAO> distinctFeatures);

    String executeHeatMapPythonScript(final List<SentenceAndFeatureDAO> distinctFeatures, final List<TermDTO> verbs, final List<TermDTO> nouns);
}