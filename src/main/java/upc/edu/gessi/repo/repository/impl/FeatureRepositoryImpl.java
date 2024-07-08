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
import upc.edu.gessi.repo.dto.Feature;
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
import upc.edu.gessi.repo.util.*;

import java.sql.Date;
import java.util.*;

@Repository
public class FeatureRepositoryImpl implements FeatureRepository {

    private final HTTPRepository repository;

    private final FeatureQueryBuilder featureQueryBuilder;


    @Autowired
    public FeatureRepositoryImpl(final @Value("${db.url}") String url,
                                 final @Value("${db.username}") String username,
                                 final @Value("${db.password}") String password,
                                 final FeatureQueryBuilder featureQueryBuild) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        featureQueryBuilder = featureQueryBuild;


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

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

    @Override
    public Map<String, Integer> findAllWithOccurrences() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAllFeaturesWithOccurrencesQuery());
        Map<String, Integer> featureOcurrencesDict = new HashMap<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if(bindings.getBinding("identifier") != null
                    && bindings.getBinding("identifier").getValue() != null
                    && bindings.getBinding("totalOccurrences") != null
                    && bindings.getBinding("totalOccurrences").getValue() != null) {
                featureOcurrencesDict.put(
                        bindings.getBinding("identifier").getValue().stringValue(),
                        Integer.valueOf(bindings.getBinding("totalOccurrences").getValue().stringValue())
                );
            }
        }
        return featureOcurrencesDict;
    }

    @Override
    public List<String> findAllDistinct() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAllDistinctFeaturesQuery());
        List<String> featuresList = new ArrayList<>();
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            if(bindings.getBinding("feature") != null
                    && bindings.getBinding("feature").getValue() != null) {
                featuresList.add(
                        bindings.getBinding("feature").getValue().stringValue());
            }
        }
        return featuresList;
    }
}
