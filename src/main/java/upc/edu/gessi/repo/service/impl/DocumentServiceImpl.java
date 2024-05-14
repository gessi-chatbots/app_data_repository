package upc.edu.gessi.repo.service.impl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.Document;
import upc.edu.gessi.repo.dto.graph.GraphDocument;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.service.DocumentService;
import upc.edu.gessi.repo.util.Utils;

import java.util.ArrayList;
import java.util.List;

@Service
@Lazy
public class DocumentServiceImpl implements DocumentService {

    private final HTTPRepository repository;

    @Autowired
    public DocumentServiceImpl(final @Value("${db.url}") String url,
                               final @Value("${db.username}") String username,
                               final @Value("${db.password}") String password) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
    }


    public List<GraphDocument> getDocumentsByApp(String app) {
        List<GraphDocument> apps = new ArrayList<>();

        String query = "PREFIX schema: <https://schema.org/>\n" +
                "\n" +
                "select * where {\n" +
                "    <" + app + "> (schema:description | schema:abstract | schema:releaseNotes) ?document .\n" +
                "    ?document schema:text ?text ;\n" +
                "              schema:disambiguatingDescription ?disDescription \n" +
                "} ";
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindings = result.next();

            IRI document = (IRI) bindings.getValue("document");
            String text = bindings.getValue("text").stringValue();
            String disDescription = bindings.getValue("disDescription").stringValue();


            GraphDocument graphDocument = new GraphDocument(document.toString(), text, disDescription);
            apps.add(graphDocument);
        }

        return apps;
    }

    @Override
    public List<Document> create(List<Document> dtos) {
        return null;
    }

    @Override
    public Document get(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public List<Document> getListed(List<String> ids) throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<Document> getAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return null;
    }

    @Override
    public List<Document> getAll() {
        return null;
    }

    @Override
    public void update(Document entity) {
    }

    @Override
    public void delete(String id) {
    }
}
