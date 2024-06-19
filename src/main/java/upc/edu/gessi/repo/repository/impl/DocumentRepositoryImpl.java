
package upc.edu.gessi.repo.repository.impl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.DigitalDocumentDTO;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.DocumentRepository;
import upc.edu.gessi.repo.util.DocumentQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentRepositoryImpl implements DocumentRepository {

    private final HTTPRepository repository;

    private final DocumentQueryBuilder documentQueryBuilder;


    @Autowired
    public DocumentRepositoryImpl(final @Value("${db.url}") String url,
                                  final @Value("${db.username}") String username,
                                  final @Value("${db.password}") String password,
                                  final DocumentQueryBuilder docQB) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        this.documentQueryBuilder = docQB;


    }

    @Override
    public Map<String, Integer> findAllDocumentTypeFeaturesWithOccurrences(final String documentType) {
        TupleQueryResult textResult =
                runSparqlQuery(documentQueryBuilder.findFeaturesWithOccurrencesByDocument(documentType));
        Map<String, Integer> results = new HashMap<>();
        while (textResult.hasNext()) {
            BindingSet bindingSet = textResult.next();
            if (bindingSet.getBinding("identifier") != null
                    && bindingSet.getBinding("identifier").getValue().stringValue() != null
                    && bindingSet.getBinding("occurrences") != null
                    && bindingSet.getBinding("occurrences").getValue().stringValue() != null) {
                String identifier = bindingSet.getBinding("identifier").getValue().stringValue();
                Integer occurrences = Integer.valueOf(bindingSet.getBinding("occurrences").getValue().stringValue());
                results.put(identifier, occurrences);
            }
        }
        return results;
    }

    @Override
    public List<String> findAllDistinctDocumentTypeFeatures(final String documentType) {
        TupleQueryResult textResult =
                runSparqlQuery(documentQueryBuilder.findDistinctFeaturesByDocument(documentType));
        List<String> results = new ArrayList<>();
        while (textResult.hasNext()) {
            BindingSet bindingSet = textResult.next();
            if (bindingSet.getBinding("identifier") != null
                    && bindingSet.getBinding("identifier").getValue().stringValue() != null) {
                String identifier = bindingSet.getBinding("identifier").getValue().stringValue();
                results.add(identifier);
            }
        }
        return results;
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

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }
}
