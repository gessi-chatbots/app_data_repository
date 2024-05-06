package upc.edu.gessi.repo.service.impl;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.MobileApplicationDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.impl.MobileApplicationRepository;
import upc.edu.gessi.repo.service.MobileApplicationService;

import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
public class MobileApplicationServiceImpl implements MobileApplicationService {

    private final MobileApplicationRepository mobileApplicationRepository;

    @Autowired
    public MobileApplicationServiceImpl(final MobileApplicationRepository appRepository) {
        mobileApplicationRepository = appRepository;
    }


    @Override
    public List<MobileApplicationDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        return mobileApplicationRepository.findAllApplicationNames();
    }

    @Override
    public List<MobileApplicationDTO> insertApps(final List<MobileApplicationDTO> mobileApplicationDTOS) {
        List<MobileApplicationDTO> insertedApps = new ArrayList<>();
        for (MobileApplicationDTO mobileApplicationDTO : mobileApplicationDTOS) {
            insertedApps.add(mobileApplicationRepository.insertApp(mobileApplicationDTO));
        }
        return insertedApps;
    }

    @Override
    public List<GraphApp> getAllApps() {
        return mobileApplicationRepository.getAllApps();
    }

    @Override
    public void addFeatures(final MobileApplicationDTO mobileApplicationDTO,
                            final IRI sub,
                            final List<Statement> statements) {
        mobileApplicationRepository.addFeature(mobileApplicationDTO, sub, statements);
    }

    @Override
    public List<MobileApplicationDTO> create(List<MobileApplicationDTO> entity) {
        return null;
    }

    @Override
    public MobileApplicationDTO get(String id) throws ObjectNotFoundException {
        return mobileApplicationRepository.findByName(id);
    }

    @Override
    public List<MobileApplicationDTO> getListed(List<String> id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<MobileApplicationDTO> getAllPaginated(boolean paginated, Integer page, Integer size) throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return mobileApplicationRepository.findAllSimplifiedPaginated(page, size);
    }

    @Override
    public List<MobileApplicationDTO> getAll() {
        try {
            return mobileApplicationRepository.findAll();
        } catch (ApplicationNotFoundException applicationNotFoundException) {
            return new ArrayList<>();
        }

    }

    @Override
    public MobileApplicationDTO update(MobileApplicationDTO entity) {
        return null;
    }

    @Override
    public Void delete() {
        return null;
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
