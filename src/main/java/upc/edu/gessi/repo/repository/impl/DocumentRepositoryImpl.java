
package upc.edu.gessi.repo.repository.impl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.DigitalDocumentDTO;
import upc.edu.gessi.repo.dto.Review.FeatureDTO;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.DocumentRepository;
import upc.edu.gessi.repo.repository.FeatureRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentRepositoryImpl implements DocumentRepository {

    private final HTTPRepository repository;


    @Autowired
    public DocumentRepositoryImpl(final @Value("${db.url}") String url,
                                  final @Value("${db.username}") String username,
                                  final @Value("${db.password}") String password) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);


    }

    @Override
    public Map<String, Integer> findAllDocumentTypeFeaturesWithOccurrences(String documentType) {
        return new HashMap<>();
    }

    @Override
    public List<String> findAllDistinctDocumentTypeFeatures(String documentType) {
        return new ArrayList<>();
    }

    @Override
    public DigitalDocumentDTO findById(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<DigitalDocumentDTO> findAll() throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<DigitalDocumentDTO> findAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return null;
    }

    @Override
    public IRI insert(DigitalDocumentDTO dto) {
        return null;
    }

    @Override
    public DigitalDocumentDTO update(DigitalDocumentDTO entity) {
        return null;
    }

    @Override
    public void delete(String id) {

    }
}
