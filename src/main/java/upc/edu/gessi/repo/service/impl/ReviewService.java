package upc.edu.gessi.repo.service.impl;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.dto.graph.GraphReview;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.impl.ReviewRepository;
import upc.edu.gessi.repo.util.Utils;

import java.util.ArrayList;
import java.util.List;


@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HTTPRepository repository;

    @Autowired
    public ReviewService(final @org.springframework.beans.factory.annotation.Value("${db.url}") String url,
                         final @org.springframework.beans.factory.annotation.Value("${db.username}") String username,
                         final @org.springframework.beans.factory.annotation.Value("${db.password}") String password,
                         final ReviewRepository reviewRep) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
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

    public List<GraphReview> getReviews(String nodeId) {
        List<GraphReview> graphReviews = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select ?review ?rating ?reviewBody where {\n" +
                "    <" + nodeId + "> schema:review ?review .\n" +
                "    ?review schema:reviewRating ?rating ;\n" +
                "            schema:reviewBody ?reviewBody\n" +
                "} ";

        TupleQueryResult result = Utils.runSparqlQuery(repository.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI review = (IRI) bindings.getValue("review");
            Integer reviewRating = Integer.valueOf(bindings.getValue("rating").stringValue());
            String reviewBody = bindings.getValue("reviewBody").stringValue();


            GraphReview graphFeature = new GraphReview(review.toString(), reviewRating, reviewBody);
            graphReviews.add(graphFeature);
        }

        return graphReviews;
    }



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
}
