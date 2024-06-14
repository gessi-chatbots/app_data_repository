package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;

@Component
public class FeatureQueryBuilder
{
    public String findAllDocumentFeaturesQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("\n");
        queryBuilder.append("SELECT distinct ?documentText WHERE {\n");
        queryBuilder.append("    ?app (schema:description | schema:abstract | schema:releaseNotes | schema:featureList) ?documentID .\n");
        queryBuilder.append("    ?documentID schema:keywords ?documentText\n");
        queryBuilder.append("}");
        return queryBuilder.toString();
    }

    public String findAllFeaturesWithOccurrencesQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?feature (COUNT(?feature) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm;\n");
        queryBuilder.append("           schema:identifier ?feature .\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?feature\n");
        return queryBuilder.toString();
    }

    public String findAllDistinctFeaturesQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT DISTINCT ?feature\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm;\n");
        queryBuilder.append("           schema:identifier ?feature .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }




}
