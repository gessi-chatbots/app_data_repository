package upc.edu.gessi.repo.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.ApplicationDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.impl.ApplicationRepository;
import upc.edu.gessi.repo.repository.impl.ReviewRepository;

import java.util.List;


@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRep) {
        reviewRepository = reviewRep;
    }

    public List findAll(boolean simplified) throws ApplicationNotFoundException {
        return simplified ? reviewRepository.findAllSimplified() : reviewRepository.findAll();
    }
    public List findAllPaginated(final Integer page, final Integer size, final boolean simplified) throws ApplicationNotFoundException {
        return simplified ? reviewRepository.findAllSimplifiedPaginated(page, size) : reviewRepository.findAll();
    }

    public List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        return  (List<ApplicationSimplifiedDTO>) reviewRepository.findAllReviewIDs();
    }

    public List findByName(final String appName) throws ApplicationNotFoundException {
        return reviewRepository.findByApplicationName(appName);
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
