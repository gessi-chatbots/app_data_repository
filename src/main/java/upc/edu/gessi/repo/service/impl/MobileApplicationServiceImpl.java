package upc.edu.gessi.repo.service.impl;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.MobileApplicationDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.impl.MobileApplicationRepository;

import java.util.ArrayList;
import java.util.List;


@Service
public class MobileApplicationServiceImpl {

    private final MobileApplicationRepository mobileApplicationRepository;

    @Autowired
    public MobileApplicationServiceImpl(final MobileApplicationRepository appRepository) {
        mobileApplicationRepository = appRepository;
    }
    public List findAll(boolean simplified) throws ApplicationNotFoundException {
        return simplified ? mobileApplicationRepository.findAllSimplified() : mobileApplicationRepository.findAll();
    }
    public List findAllPaginated(final Integer page, final Integer size) throws ApplicationNotFoundException {
        return mobileApplicationRepository.findAllSimplifiedPaginated(page, size);
    }
    public List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        return  (List<ApplicationSimplifiedDTO>) mobileApplicationRepository.findAllApplicationNames();
    }
    public MobileApplicationDTO findByName(final String appName) throws ObjectNotFoundException {
        return mobileApplicationRepository.findByName(appName);
    }

    public List<MobileApplicationDTO> insertApps(final List<MobileApplicationDTO> mobileApplicationDTOS) {
        List<MobileApplicationDTO> insertedApps = new ArrayList<>();
        for (MobileApplicationDTO mobileApplicationDTO : mobileApplicationDTOS) {
            insertedApps.add(mobileApplicationRepository.insertApp(mobileApplicationDTO));
        }
        return insertedApps;
    }

    public List<GraphApp> getAllApps() {
        return mobileApplicationRepository.getAllApps();
    }

    public void addFeatures(final MobileApplicationDTO mobileApplicationDTO,
                            final IRI sub,
                            final List<Statement> statements) {
        mobileApplicationRepository.addFeaturesToApplication(mobileApplicationDTO, sub, statements);
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
