package upc.edu.gessi.repo.service;


import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.ApplicationDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.impl.ApplicationRepository;

import java.util.List;


@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(ApplicationRepository appRepository) {
        applicationRepository = appRepository;
    }

    public List findAll(boolean simplified) throws ApplicationNotFoundException {
        return simplified ? applicationRepository.findAllSimplified() : applicationRepository.findAll();
    }
    public List findAllPaginated(final Integer page, final Integer size, final boolean simplified) throws ApplicationNotFoundException {
        return simplified ? applicationRepository.findAllSimplifiedPaginated(page, size) : applicationRepository.findAll();
    }

    public List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        return  (List<ApplicationSimplifiedDTO>) applicationRepository.findAllApplicationNames();
    }

    public ApplicationDTO findByName(final String appName) throws ApplicationNotFoundException {
        return applicationRepository.findByName(appName);
    }

    public void insertApps(final List<ApplicationDTO> applicationDTOS) {
        for (ApplicationDTO applicationDTO : applicationDTOS) {
            applicationRepository.insertApp(applicationDTO);
        }

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
    }*/
}
