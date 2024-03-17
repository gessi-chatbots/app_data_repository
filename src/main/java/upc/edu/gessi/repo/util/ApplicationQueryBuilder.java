package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;

@Component
public class ApplicationQueryBuilder
{
    public String findAllSimplifiedQuery(Integer page, Integer size) {
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

    public String findAllQuery() {
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

    public String findAllApplicationNamesQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name ?package\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:name ?name ;\n");
        queryBuilder.append("       schema:identifier ?package .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String findByNameQuery(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?predicate ?object ?text\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?s schema:name \"").append(appName).append("\" .\n");
        queryBuilder.append("  ?s ?predicate ?object .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }




}