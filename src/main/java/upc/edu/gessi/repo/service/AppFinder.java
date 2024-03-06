package upc.edu.gessi.repo.service;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.App;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Deprecated
public class AppFinder {

    private final Repository repository;

    public AppFinder(@org.springframework.beans.factory.annotation.Value("${db.url}") String url) {
        repository = new HTTPRepository(url);
    }

    public List<App> retrieveAllApps()  throws ClassNotFoundException, IllegalAccessException {
        RepositoryConnection repoConnection = repository.getConnection();
        String query = "PREFIX gessi: <https://schema.org/MobileApplication/> \n" +
                "\n" +
                "SELECT ?description\n" +
                "WHERE\n" +
                "{ gessi:Google_Maps gessi:description ?description . }\n" ;
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        TupleQueryResult result = tupleQuery.evaluate();

        Class<?> c = Class.forName("upc.edu.gessi.repo.dto.App");
        Field[] fieldList = c.getDeclaredFields();

        List<App> apps = new ArrayList<>();
        while (result.hasNext()) {
           //TODO
            BindingSet bindings = result.next();
            App app = new App();
        }
        repoConnection.close();
        return apps;
    }

    public App retrieveAppByName(String appName) throws ApplicationNotFoundException, ClassNotFoundException, IllegalAccessException {
        RepositoryConnection repoConnection = repository.getConnection();
        String query = "PREFIX gessi: <http://gessi.upc.edu/app/> SELECT ?x ?y ?z WHERE {gessi:"+appName+" ?y ?z}" ;
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        TupleQueryResult result = tupleQuery.evaluate();
        if (result.getBindingNames().isEmpty()) {
            throw new ApplicationNotFoundException("No application was found with the app name given");
        }
        App res = new App();
        Class<?> c = Class.forName("upc.edu.gessi.repo.dto.App");
        Field[] fieldList = c.getDeclaredFields();
        List<Map<String,String>> reviews = new ArrayList<>();
        Field rev = null;
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            Value pred = bindings.getValue("y");
            Value obj = bindings.getValue("z");
            String object = obj.stringValue();
            String predicate = pred.stringValue();
            for (Field f : fieldList) {
                if (predicate.toLowerCase().endsWith(f.getName().toLowerCase())) {
                    if (predicate.toLowerCase().endsWith("reviews")) {
                        Map<String,String> aux = new HashMap<>();
                        aux.put("review",object);
                        aux.put("reply",null);
                        reviews.add(aux);
                        rev = f;
                    } else {
                        f.setAccessible(true);
                        f.set(res, object);
                    }
                }
            }
        }
        if (rev != null) {
            rev.setAccessible(true);
            rev.set(res, reviews);
        }
        repoConnection.close();
        return res;
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
