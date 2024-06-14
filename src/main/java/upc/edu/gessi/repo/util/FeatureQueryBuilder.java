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



}
