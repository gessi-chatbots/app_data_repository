package upc.edu.gessi.repo.service.impl;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.Executor;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import upc.edu.gessi.repo.service.GraphDBService;
import upc.edu.gessi.repo.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class GraphDBServiceImpl implements GraphDBService {

    private Logger logger = LoggerFactory.getLogger(GraphDBServiceImpl.class);

    private HTTPRepository repository;

    private int count;

    public GraphDBServiceImpl(@Value("${db.url}") String url,
                              @Value("${db.username}") String username,
                              @Value("${db.password}") String password) {
        repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
    }

    @Override
    public void updateRepository(String url) {
        repository = new HTTPRepository(url);
    }

    @Override
    public int getCount() {return count;}

    @Override
    public void deleteSameAsRelations() {
        String query = "delete where { \n" +
                "    ?x <https://schema.org/sameAs> ?z .\n" +
                "}";
        TupleQueryResult result = Utils.runSparqlSelectQuery(repository.getConnection(), query);

        int counter = 0;
        while (result.next() != null) ++counter;

        logger.info(counter + " similarity relations deleted");
    }

    @Override
    public void exportRepository(String fileName) throws Exception {
        RepositoryConnection connection = repository.getConnection();
        FileOutputStream outputStream = new FileOutputStream("src/main/resources/exports/" + fileName);
        RDFWriter writer = Rio.createWriter(RDFFormat.RDFJSON, outputStream);
        connection.exportStatements(null, null, null, false, writer);
        IOUtils.closeQuietly(outputStream);
    }


    @Override
    public void insertRDF(MultipartFile file) throws Exception {
        // Parse the Turtle file into an RDF model
        try (InputStream inputStream = file.getInputStream()) {
            Model model = Rio.parse(inputStream, "", RDFFormat.TURTLE);
            RepositoryConnection repoConnection = repository.getConnection();
            repoConnection.begin();
            repoConnection.add(model);
            repoConnection.commit();
            repoConnection.close();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void insertRML(String jsonFolder, File mappingFile) throws Exception {
        InputStream mappingStream = new FileInputStream(mappingFile);
        QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

        // Create RecordsGenerator for JSON data
        RecordsFactory recordsFactory = new RecordsFactory(jsonFolder);

        QuadStore outputStore = new RDF4JStore();
        // Set up the functions used during the mapping
        Agent functionAgent = AgentFactory.createFromFnO(
                "fno/functions_idlab.ttl",
                "fno/functions_idlab_classes_java_mapping.ttl",
                "grel_java_mapping.ttl",
                "functions_grel.ttl");

        // Create the Executor
        Executor executor = new Executor(rmlStore, recordsFactory, outputStore, be.ugent.rml.Utils.getBaseDirectiveTurtle(mappingStream), functionAgent);
        // Execute the mapping
        QuadStore result = executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        // Optionally, you can convert the result to an RDF4J Model
        System.out.println(result);

    }

    /*private static Model convertQuadStoreToRDF4JModel(QuadStore quadStore) {
        Model rdf4jModel = new LinkedHashModel();

        quadStore.ite(quad -> {
            rdf4jModel.add(
                    SimpleValueFactory.getInstance().createIRI(quad.getSubject().getValue()),
                    SimpleValueFactory.getInstance().createIRI(quad.getPredicate().getValue()),
                    SimpleValueFactory.getInstance().createLiteral(quad.getObject().getValue()),
                    SimpleValueFactory.getInstance().createIRI(quad.getGraph().getValue())
            );
        });

        return rdf4jModel;
    }*/
}
