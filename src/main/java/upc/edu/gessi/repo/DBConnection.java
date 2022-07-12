package upc.edu.gessi.repo;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModelFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class DBConnection {
    private String rd4jEndpoint;
    private Repository repository = null;

    private ValueFactory factory = SimpleValueFactory.getInstance();

    public DBConnection(String rd4jEndpoint) {
        this.rd4jEndpoint = rd4jEndpoint;
        repository = new HTTPRepository(this.rd4jEndpoint);
    }

    public void insertData(String subject, String predicate, String object) {
        IRI sub = factory.createIRI(subject);
        IRI pred = factory.createIRI(predicate);
        Literal name = factory.createLiteral(object);
        Statement statement = factory.createStatement(sub,pred,name);
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
        model.add(statement);
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(model);
        repoConnection.close();
    }

    private String extractValue(Map<String,String> map, String key) {
        return map.get(key);
    }

    public void insertApp(App app, String name) throws ClassNotFoundException {
        IRI sub = factory.createIRI("http://gessi.upc.edu/app/" + name);
        Class<?> c = Class.forName("upc.edu.gessi.repo.App");
        Field[] fieldList = c.getDeclaredFields();
        Method[] methodList = c.getMethods();
        ModelFactory modelFactory = new TreeModelFactory();
        Model model = modelFactory.createEmptyModel();
        for (Field f : fieldList) {
            IRI pred = factory.createIRI("https://schema.org/" + f.getName());
            Object obj = "";
            for (Method m : methodList) {
                if (m.getName().length() == (f.getName().length() + 3) && m.getName().toLowerCase().equals("get"+f.getName().toLowerCase())) {
                    try {
                        obj = m.invoke(app);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (obj instanceof List<?>) {
                for (Object s : Collections.unmodifiableList((List) obj)) {
                    Map<String,String> map = (Map<String,String>) s;
                    String aux = map.get("review");
                    Literal object = factory.createLiteral(aux);
                    Statement statement = factory.createStatement(sub, pred, object);
                    model.add(statement);
                }
            }
            else {
                Literal object = factory.createLiteral((String) obj);
                Statement statement = factory.createStatement(sub, pred, object);
                model.add(statement);
            }

        }
        RepositoryConnection repoConnection = repository.getConnection();
        repoConnection.add(model);
        repoConnection.close();
    }




}
