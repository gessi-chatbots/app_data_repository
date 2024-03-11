package upc.edu.gessi.repo.repository.impl;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import upc.edu.gessi.repo.dto.ApplicationDTO;
import upc.edu.gessi.repo.dto.ApplicationSimplifiedDTO;
import upc.edu.gessi.repo.exception.ApplicationNotFoundException;
import upc.edu.gessi.repo.repository.RdfRepository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ApplicationRepository <T> implements RdfRepository<ApplicationDTO> {

    private final org.eclipse.rdf4j.repository.Repository repository;

    public ApplicationRepository(final @Value("${db.url}") String url) {
        repository = new HTTPRepository(url);
    }
    private String findAllSimplifiedQuery(Integer page, Integer size) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name ?authorName ?reviewCount\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name  ?authorName\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:abstract ?abstract ;\n");
        queryBuilder.append("           schema:author ?author .\n");
        queryBuilder.append("      ?author schema:author ?authorName .\n");
        queryBuilder.append("    }\n");
        if (size != null) {
            queryBuilder.append("    LIMIT ").append(size).append("\n");
        }
        if (page != null && size != null) {
            int offset = (page - 1) * size;
            queryBuilder.append("    OFFSET ").append(offset).append("\n");
        }
        queryBuilder.append("  }\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name (STR(COUNT(?review)) as ?reviewCount)\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:review ?review .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    GROUP BY ?name\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  FILTER (?name = ?name)\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    private String findAllQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name ?description ?authorName ?reviewCount\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name ?description ?authorName\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:abstract ?abstract ;\n");
        queryBuilder.append("           schema:author ?author .\n");
        queryBuilder.append("      ?abstract schema:text ?description .\n");
        queryBuilder.append("      ?author schema:author ?authorName .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    LIMIT 20\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name (STR(COUNT(?review)) as ?reviewCount)\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:review ?review .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    GROUP BY ?name\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  FILTER (?name = ?name)\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    private String findAllApplicationNamesQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:name ?name ;\n");
        queryBuilder.append("       schema:identifier ?package .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    private String findByNameQuery(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT DISTINCT ?name\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:name ?name ;\n");
        queryBuilder.append("       schema:identifier ?package .\n");
        queryBuilder.append("    FILTER (STRSTARTS(STR(?name), \"").append(appName).append("\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("LIMIT 1");
        return queryBuilder.toString();
    }

    private TupleQueryResult runSparqlQuery(final String query) {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }
    private ApplicationSimplifiedDTO bindingSetToApplicationSimplifiedDTO(final BindingSet bindings) {
        ApplicationSimplifiedDTO applicationSimplifiedDTO = new ApplicationSimplifiedDTO();
        if (bindings.getBinding("name") != null
                && bindings.getBinding("name").getValue() != null) {
            applicationSimplifiedDTO.setName(
                    bindings.getBinding("name").getValue().stringValue());
        }
        if (bindings.getBinding("authorName") != null
                && bindings.getBinding("authorName").getValue() != null) {
            applicationSimplifiedDTO.setAuthor(
                    bindings.getBinding("authorName").getValue().stringValue());
        }
        if (bindings.getBinding("reviewCount") != null
                && bindings.getBinding("reviewCount").getValue() != null) {
            applicationSimplifiedDTO.setReviewCount(
                    Integer.valueOf(
                                    bindings.getBinding("reviewCount").getValue().stringValue()
                    )
            );
        }

        return applicationSimplifiedDTO;
    }

    private ApplicationDTO bindingSetToApplicationDTO(final BindingSet bindings) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        if (bindings.getBinding("name") != null
                && bindings.getBinding("name").getValue() != null) {
            applicationDTO.setName(String.valueOf(bindings.getBinding("name").getValue()));
        }
        return applicationDTO;
    }
    @Override
    public List<ApplicationDTO> findAll() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllQuery());
        List<ApplicationDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationDTO(result.next()));
        }
        return applicationDTOS;
    }

    public List<ApplicationSimplifiedDTO> findAllSimplified() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllSimplifiedQuery(null, null));
        List<ApplicationSimplifiedDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationSimplifiedDTO(result.next()));
        }
        return applicationDTOS;
    }

    public List<ApplicationSimplifiedDTO> findAllSimplifiedPaginated(final Integer page,
                                                                     final Integer size) throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllSimplifiedQuery(page, size));
        List<ApplicationSimplifiedDTO> applicationDTOS = new ArrayList<>();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationSimplifiedDTO(result.next()));
        }
        return applicationDTOS;
    }

    public List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findAllApplicationNamesQuery());
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        List<ApplicationSimplifiedDTO> applicationDTOS = new ArrayList<>();
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationSimplifiedDTO(result.next()));
        }
        return applicationDTOS;
    }

    public ApplicationDTO findByName(final String appName) throws ApplicationNotFoundException {
        TupleQueryResult result = runSparqlQuery(findByNameQuery(appName));
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found with the given app name");
        }
        return bindingSetToApplicationDTO(result.next());
    }
}
