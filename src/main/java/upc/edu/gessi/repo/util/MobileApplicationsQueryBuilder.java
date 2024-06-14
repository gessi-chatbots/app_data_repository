package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;

@Component
public class MobileApplicationsQueryBuilder
{
    public String findAllPaginatedSimplifiedQuery(Integer page, Integer size) {
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

    public String findAllPaginatedQuery(Integer page, Integer size) {
        // TODO fix how to do pagination, this does not work
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?predicate ?object\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:name ?name ;\n");
        queryBuilder.append("       ?predicate ?object .\n");
        queryBuilder.append("}\n");
        if (size != null) {
            queryBuilder.append("LIMIT ").append(size).append("\n");
        }
        if (page != null && size != null) {
            int offset = (page - 1) * size;
            queryBuilder.append("OFFSET ").append(offset).append("\n");
        }
        return queryBuilder.toString();
    }

    public String findAllQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?subject ?predicate ?object\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?subject rdf:type schema:MobileApplication .\n");
        queryBuilder.append("  ?subject ?predicate ?object .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String findAllApplicationsBasicDataQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?name ?package ?reviewCount\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name ?package\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:name ?name ;\n");
        queryBuilder.append("           schema:identifier ?package .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?name (COUNT(?review) AS ?reviewCount)\n");
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

    public String findByNameQuery(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?predicate ?object\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?s schema:name \"").append(appName).append("\" .\n");
        queryBuilder.append("  ?s ?predicate ?object .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String deleteByNameQuery(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("DELETE\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?s schema:name \"").append(appName).append("\" .\n");
        queryBuilder.append("  ?s ?p ?o .\n");
        queryBuilder.append("}");
        return queryBuilder.toString();
    }

    public String findAllDistinctFeaturesByAppNameQuery(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT DISTINCT ?feature\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appName { \"").append(appName).append("\" }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:name ?appName;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        schema:keywords ?keyword .\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm;\n");
        queryBuilder.append("           schema:identifier ?feature .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String findAllFeaturesWithOccurrencesAppNameQuery(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?feature (COUNT(?feature) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appName { \"").append(appName).append("\" }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:name ?appName;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        schema:keywords ?keyword .\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm;\n");
        queryBuilder.append("           schema:identifier ?feature .\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?feature\n");
        return queryBuilder.toString();
    }

    public String findAllMobileAppIdentifiersQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?appIdentifier\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("    ?mobileApp rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("               schema:identifier ?appIdentifier.\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }




}
