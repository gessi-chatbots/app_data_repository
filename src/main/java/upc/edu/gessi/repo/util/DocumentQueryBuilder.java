package upc.edu.gessi.repo.util;


import org.springframework.stereotype.Component;

@Component
public class DocumentQueryBuilder {
    public String findFeaturesWithOccurrencesByDocument(final String document) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n\n");
        queryBuilder.append("SELECT ?identifier (COUNT(?identifier) AS ?occurrences)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?subject rdf:type schema:DigitalDocument ;\n");
        queryBuilder.append("           schema:disambiguatingDescription \"").append(document).append("\" ;\n");
        queryBuilder.append("           schema:keywords ?keywords .\n");
        queryBuilder.append("  ?keywords rdf:type schema:DefinedTerm ;\n");
        queryBuilder.append("            schema:identifier ?identifier .\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?identifier\n");
        return queryBuilder.toString();
    }
    public String findDistinctFeaturesByDocument(final String document) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n\n");
        queryBuilder.append("SELECT DISTINCT ?identifier\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?subject rdf:type schema:DigitalDocument ;\n");
        queryBuilder.append("           schema:disambiguatingDescription \"").append(document).append("\" ;\n");
        queryBuilder.append("           schema:keywords ?keywords .\n");
        queryBuilder.append("  ?keywords rdf:type schema:DefinedTerm ;\n");
        queryBuilder.append("            schema:identifier ?identifier .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

}
