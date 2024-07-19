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
import upc.edu.gessi.repo.dao.ApplicationPropDocStatisticDAO;
import upc.edu.gessi.repo.dao.SentenceAndFeatureDAO;
import upc.edu.gessi.repo.dto.Review.FeatureDTO;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.FeatureRepository;
import upc.edu.gessi.repo.util.*;

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
    public List<SentenceAndFeatureDAO> findAllDistinct() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAllDistinctFeaturesQuery());
        List<SentenceAndFeatureDAO> featuresList = new ArrayList<>();
        while (result.hasNext()) {
            SentenceAndFeatureDAO sentenceAndFeatureDAO = new SentenceAndFeatureDAO();
            BindingSet bindings = result.next();
            if(bindings.getBinding("feature") != null
                    && bindings.getBinding("feature").getValue() != null) {
                sentenceAndFeatureDAO.setFeature((
                        bindings.getBinding("feature").getValue().stringValue()));
            }
        }
        return featuresList;
    }

    @Override
    public List<ApplicationPropDocStatisticDAO> findAllApplicationsStatistics() {
        TupleQueryResult result = runSparqlQuery(featureQueryBuilder.findAppStatistics());
        List<ApplicationPropDocStatisticDAO> applicationsStatistics = new ArrayList<>();
        while (result.hasNext()) {
            ApplicationPropDocStatisticDAO applicationPropDocStatisticDAO = new ApplicationPropDocStatisticDAO();
            BindingSet bindings = result.next();

            if(bindings.getBinding("appName") != null
                    && bindings.getBinding("appName").getValue() != null) {
                applicationPropDocStatisticDAO.setIdentifier(bindings.getBinding("appName").getValue().stringValue());
            }

            if(bindings.getBinding("countReviewFeatures") != null
                    && bindings.getBinding("countReviewFeatures").getValue() != null) {
                applicationPropDocStatisticDAO.setReviewFeaturesCount(
                        Integer.valueOf(bindings.getBinding("countReviewFeatures").getValue().stringValue())
                );
            }

            if(bindings.getBinding("countSummaryFeatures") != null
                    && bindings.getBinding("countSummaryFeatures").getValue() != null) {
                applicationPropDocStatisticDAO.setSummaryFeaturesCount(
                        Integer.valueOf(bindings.getBinding("countSummaryFeatures").getValue().stringValue())
                );
            }

            if(bindings.getBinding("countDescriptionFeatures") != null
                    && bindings.getBinding("countDescriptionFeatures").getValue() != null) {
                applicationPropDocStatisticDAO.setDescriptionFeaturesCount(
                        Integer.valueOf(bindings.getBinding("countDescriptionFeatures").getValue().stringValue())
                );
            }
            applicationsStatistics.add(applicationPropDocStatisticDAO);
        }
        return applicationsStatistics;
    }
}
