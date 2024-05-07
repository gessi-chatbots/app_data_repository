package upc.edu.gessi.repo.service.impl;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationBasicDataDTO;
import upc.edu.gessi.repo.dto.MobileApplication.MobileApplicationFullDataDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.MobileApplicationRepository;
import upc.edu.gessi.repo.repository.RepositoryFactory;
import upc.edu.gessi.repo.repository.impl.MobileApplicationRepositoryImpl;
import upc.edu.gessi.repo.service.MobileApplicationService;
import upc.edu.gessi.repo.util.Utils;

import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
public class MobileApplicationServiceImpl implements MobileApplicationService {

    private final RepositoryFactory repositoryFactory;

    @Autowired
    public MobileApplicationServiceImpl(final RepositoryFactory repoFact) {
        repositoryFactory = repoFact;
    }

    @Override
    public List<MobileApplicationBasicDataDTO> getAllBasicData() throws MobileApplicationNotFoundException {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAllApplicationsBasicData();
    }

    @Override
    public List<GraphApp> getAllApps() {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).getAllApps();
    }

    @Override
    public void addFeatures(final MobileApplicationFullDataDTO mobileApplicationFullDataDTO,
                            final IRI sub,
                            final List<Statement> statements) {
        ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).addFeature(mobileApplicationFullDataDTO, sub, statements);
    }

    @Override
    public List<MobileApplicationFullDataDTO> create(List<MobileApplicationFullDataDTO> dtos) {
        List<MobileApplicationFullDataDTO> insertedApps = new ArrayList<>();
        for (MobileApplicationFullDataDTO mobileApplicationFullDataDTO : dtos) {
            // Insert App
            insertedApps.add(((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).insert(mobileApplicationFullDataDTO));
            // Insert App reviews
            // TODO use review Service

            // Insert app Features
            // TODO use Feature Service

            // Insert App digital Documents
            // TODO use digital document service
        }
        return insertedApps;
    }

    @Override
    public MobileApplicationFullDataDTO get(String id) throws ObjectNotFoundException {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findByName(Utils.sanitizeString(id));
    }

    @Override
    public List<MobileApplicationFullDataDTO> getListed(List<String> ids) throws ObjectNotFoundException {
        List<MobileApplicationFullDataDTO> mobileApplicationFullDataDTOS = new ArrayList<>();
        for (String id : ids) {
            try {
                mobileApplicationFullDataDTOS.add(((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findByName(id));
            } catch (ObjectNotFoundException ignored) {
                // do nothing, refactor in future to error tracking
            }

        }
        return mobileApplicationFullDataDTOS;
    }

    @Override
    public List<MobileApplicationFullDataDTO> getAllPaginated(Integer page, Integer size) throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAllPaginated(page, size);
    }

    @Override
    public List<MobileApplicationBasicDataDTO> getAllBasicDataPaginated(Integer page, Integer size) throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAllBasicDataPaginated(page, size);
    }
    @Override
    public List<MobileApplicationFullDataDTO> getAll() {
        try {
            return ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).findAll();
        } catch (MobileApplicationNotFoundException mobileApplicationNotFoundException) {
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void update(MobileApplicationFullDataDTO entity) {
        ((MobileApplicationRepository) useRepository(MobileApplicationRepository.class)).update(entity);
    }

    @Override
    public Void delete() {
        return null;
    }
    private Object useRepository(Class<?> clazz) {
        return repositoryFactory.createRepository(clazz);
    }
    /*
        public List<String> getResultsContaining(String text) {
            RepositoryConnection repoConnection = repository.getConnection();
            String query = "PREFIX gessi: <http://gessi.upc.edu/app/> SELECT ?x ?y ?z " +
                                                                        "WHERE {?x ?y ?z .FILTER regex(str(?z), \""+text+"\")}" ;
            TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
            List<String> resultList = new ArrayList<>();
            TupleQueryResult result = tupleQuery.evaluate();
            while (result.hasNext()) {  // iterate over the result
                BindingSet bindingSet = result.next();
                Value valueOfX = bindingSet.getValue("x");
                Value valueOfY = bindingSet.getValue("y");
                Value valueOfZ = bindingSet.getValue("z");
                resultList.add(valueOfZ.stringValue());
            }
            return resultList;
        }
    */
}
