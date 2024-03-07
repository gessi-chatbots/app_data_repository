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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ApplicationRepository implements RdfRepository<ApplicationDTO> {

    private final org.eclipse.rdf4j.repository.Repository repository;

    public ApplicationRepository(final @Value("${db.url}") String url) {
        repository = new HTTPRepository(url);
    }

    private ApplicationSimplifiedDTO bindingSetToApplicationSimplifiedDTO(final BindingSet bindings) {

        ApplicationSimplifiedDTO applicationSimplifiedDTO = new ApplicationSimplifiedDTO();
        if (bindings.getBinding("name") != null
                && bindings.getBinding("name").getValue() != null) {
            applicationSimplifiedDTO.setName(String.valueOf(bindings.getBinding("name").getValue()));
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

    private String findAllQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:name ?name ;\n");
        queryBuilder.append("       schema:identifier ?package .\n");
        queryBuilder.append("}\n");
        String query = queryBuilder.toString();
        return query;
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
        String query = queryBuilder.toString();
        return query;
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
        String query = queryBuilder.toString();
        return query;
    }

    @Override
    public List<ApplicationDTO> findAll() throws ApplicationNotFoundException {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(findAllQuery());
        TupleQueryResult result = tupleQuery.evaluate();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        return null;
    }

    public List<ApplicationSimplifiedDTO> findAllApplicationNames() throws ApplicationNotFoundException {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(findAllApplicationNamesQuery());
        TupleQueryResult result = tupleQuery.evaluate();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found");
        }
        List<ApplicationSimplifiedDTO> applicationDTOS = new ArrayList<>();
        while (result.hasNext()) {
            applicationDTOS.add(bindingSetToApplicationSimplifiedDTO(result.next()));
        }

        return applicationDTOS;
    }

    public ApplicationDTO findByName(final String appName) throws IllegalAccessException, ClassNotFoundException, ApplicationNotFoundException {
        RepositoryConnection repoConnection = repository.getConnection();
        TupleQuery tupleQuery = repoConnection.prepareTupleQuery(findByNameQuery(appName));
        TupleQueryResult result = tupleQuery.evaluate();
        if (!result.hasNext()) {
            throw new ApplicationNotFoundException("No applications were found with the given app name");
        }
        ApplicationDTO res = new ApplicationDTO();
        Class<?> c = Class.forName("upc.edu.gessi.repo.dto.ApplicationDTO");
        Field[] fieldList = c.getDeclaredFields();
        List<Map<String,String>> reviews = new ArrayList<>();
        Field rev = null;
        while (result.hasNext()) {
            BindingSet bindings = result.next();
            org.eclipse.rdf4j.model.Value pred = bindings.getValue("y");
            org.eclipse.rdf4j.model.Value obj = bindings.getValue("z");
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
}
