package upc.edu.gessi.repo.service.impl;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.ApplicationDataDTO;
import upc.edu.gessi.repo.dto.CompleteApplicationDataDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.graph.GraphApp;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.impl.ApplicationRepository;

import java.util.List;


@Service
public class ApplicationServiceImpl {

    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationServiceImpl(final ApplicationRepository appRepository) {
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

    public ApplicationDataDTO findByName(final String appName) throws ApplicationNotFoundException {
        return applicationRepository.findByName(appName);
    }

    public void insertApps(final List<CompleteApplicationDataDTO> completeApplicationDataDTOS) {
        for (CompleteApplicationDataDTO completeApplicationDataDTO : completeApplicationDataDTOS) {
            applicationRepository.insertApp(completeApplicationDataDTO);
        }

    }

    public List<GraphApp> getAllApps() {
        return applicationRepository.getAllApps();
    }

    public void addFeatures(final CompleteApplicationDataDTO completeApplicationDataDTO,
                            final IRI sub,
                            final List<Statement> statements) {
        applicationRepository.addFeaturesToApplication(completeApplicationDataDTO, sub, statements);
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
