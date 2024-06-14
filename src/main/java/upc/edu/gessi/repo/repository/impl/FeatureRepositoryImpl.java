package upc.edu.gessi.repo.repository.impl;

import org.apache.commons.text.WordUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.DocumentType;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.Review.FeatureDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.SimilarityApp;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.MobileApplications.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.exception.MobileApplications.NoMobileApplicationsFoundException;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.FeatureRepository;
import upc.edu.gessi.repo.repository.MobileApplicationRepository;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.util.MobileApplicationsQueryBuilder;
import upc.edu.gessi.repo.util.ReviewQueryBuilder;
import upc.edu.gessi.repo.util.SchemaIRI;
import upc.edu.gessi.repo.util.Utils;

import java.sql.Date;
import java.util.*;

@Repository
public class FeatureRepositoryImpl implements FeatureRepository {

    private final HTTPRepository repository;


    @Autowired
    public FeatureRepositoryImpl(final @Value("${db.url}") String url,
                                 final @Value("${db.username}") String username,
                                 final @Value("${db.password}") String password) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);


    }


    @Override
    public FeatureDTO findById(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<FeatureDTO> findAll() throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<FeatureDTO> findAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return null;
    }

    @Override
    public IRI insert(FeatureDTO dto) {
        return null;
    }

    @Override
    public FeatureDTO update(FeatureDTO entity) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public Map<String, Integer> findAllWithOccurrences() {
        return new HashMap<>();
    }

    @Override
    public List<String> findAllDistinct() {
        return new ArrayList<>();
    }
}
