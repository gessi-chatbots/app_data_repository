package upc.edu.gessi.repo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface GraphDBService {
    void updateRepository(String url);

    int getCount();

    void deleteSameAsRelations();

    void exportRepository(String fileName) throws Exception;

    void insertRDF(MultipartFile file) throws Exception;

    void insertRML(String jsonFolder, File mappingFile) throws Exception;
}
