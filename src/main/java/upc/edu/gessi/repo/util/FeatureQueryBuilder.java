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
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?identifier (SUM(?occurrences) AS ?totalOccurrences)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?identifierDigitalDocument (COUNT(?identifierDigitalDocument) AS ?occurrences)\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?digitalDocument rdf:type schema:DigitalDocument ;\n");
        queryBuilder.append("                       schema:keywords ?keywords .\n");
        queryBuilder.append("      ?keywords rdf:type schema:DefinedTerm ;\n");
        queryBuilder.append("                schema:identifier ?identifierDigitalDocument .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    GROUP BY ?identifierDigitalDocument\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  UNION\n");
        queryBuilder.append("  {\n");
        queryBuilder.append("    SELECT ?identifierReview (COUNT(?identifierReview) AS ?occurrences)\n");
        queryBuilder.append("    WHERE {\n");
        queryBuilder.append("      ?review rdf:type schema:Review;\n");
        queryBuilder.append("              schema:keywords ?definedReviewTerm.\n");
        queryBuilder.append("      ?definedReviewTerm rdf:type schema:DefinedTerm;\n");
        queryBuilder.append("                         schema:identifier ?identifierReview .\n");
        queryBuilder.append("    }\n");
        queryBuilder.append("    GROUP BY ?identifierReview\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY (COALESCE(?identifierDigitalDocument, ?identifierReview) AS ?identifier)\n");
        queryBuilder.append("ORDER BY DESC(?totalOccurrences)\n");
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


    public String findAppStatistics() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?appName (COUNT(DISTINCT ?reviewFeatures) AS ?countReviewFeatures) (COUNT(DISTINCT ?summaryFeatures) AS ?countSummaryFeatures) (COUNT(DISTINCT ?descriptionFeatures) AS ?countDescriptionFeatures)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:identifier ?appName;\n");
        queryBuilder.append("       schema:abstract ?summary;\n");
        queryBuilder.append("       schema:description ?description ;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review .\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?part schema:keywords ?reviewFeatures .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?summary schema:keywords ?summaryFeatures .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?description schema:keywords ?descriptionFeatures .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?appName\n");
        return queryBuilder.toString();
    }

    public String findAppDescriptionsFeaturesWithContextQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?appIdentifier ?descriptionText ?featureIdentifier\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:identifier ?appIdentifier ;\n");
        queryBuilder.append("       schema:description ?description .\n");
        queryBuilder.append("  ?description rdf:type schema:DigitalDocument ;\n");
        queryBuilder.append("               schema:text ?descriptionText ;\n");
        queryBuilder.append("               schema:keywords ?feature .\n");
        queryBuilder.append("  ?feature rdf:type schema:DefinedTerm ;\n");
        queryBuilder.append("           schema:identifier ?featureIdentifier .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }
    public String findAppSummariesFeaturesWithContextQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?appIdentifier ?summaryText ?featureIdentifier\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:identifier ?appIdentifier ;\n");
        queryBuilder.append("       schema:abstract ?summary .\n");
        queryBuilder.append("  ?summary rdf:type schema:DigitalDocument ;\n");
        queryBuilder.append("               schema:text ?summaryText ;\n");
        queryBuilder.append("               schema:keywords ?feature .\n");
        queryBuilder.append("  ?feature rdf:type schema:DefinedTerm ;\n");
        queryBuilder.append("           schema:identifier ?featureIdentifier .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }
    public String findAppReviewsFeaturesWithContextQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("\n");
        queryBuilder.append("SELECT ?appIdentifier ?reviewText ?sentenceId ?featureIdentifier\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("       schema:identifier ?appIdentifier ;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review ;\n");
        queryBuilder.append("         schema:reviewBody ?reviewText ;\n");
        queryBuilder.append("         schema:additionalProperty ?sentences .\n");
        queryBuilder.append("  ?sentences rdf:type schema:Review ;\n");
        queryBuilder.append("             schema:identifier ?sentenceId ;\n");
        queryBuilder.append("             schema:keywords ?features .\n");
        queryBuilder.append("  ?features rdf:type schema:DefinedTerm ;\n");
        queryBuilder.append("            schema:identifier ?featureIdentifier .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }


}
