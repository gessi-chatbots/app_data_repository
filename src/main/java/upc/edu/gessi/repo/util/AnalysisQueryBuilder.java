package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class AnalysisQueryBuilder
{

    public String findFeaturesByAppName(final String appName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT DISTINCT ?feature\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appName { \"" + appName + "\" }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:name ?appName;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        schema:keywords ?keyword .\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature .\n");
        queryBuilder.append("}\n");

        return queryBuilder.toString();
    }
    public static String findStatisticBetweenDates(String appName, Date startDate, Date endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\n");
        queryBuilder.append("SELECT ?sentiment ?feature ?date\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appName {\n");
        queryBuilder.append("    \"" + appName + "\"\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:name ?appName;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:hasPart ?part ;\n");
        queryBuilder.append("          schema:datePublished ?date .\n");
        queryBuilder.append("  ?part rdf:type schema:CreativeWork;\n");
        queryBuilder.append("        schema:keywords ?keyword;\n");
        queryBuilder.append("        schema:potentialAction ?potentialAction.\n");
        queryBuilder.append("  ?potentialAction rdf:type schema:ReactAction; \n");
        queryBuilder.append("                   schema:identifier ?sentiment.\n");
        queryBuilder.append("  FILTER (?sentiment IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature.\n\n");
        queryBuilder.append("  FILTER(?date >= \"" + dateFormat.format(startDate) + "\"^^xsd:dateTime &&\n");
        queryBuilder.append("         ?date <= \"" + dateFormat.format(endDate) + "\"^^xsd:dateTime)\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?sentiment ?feature ?date\n");
        queryBuilder.append("ORDER BY ASC(?date)\n");

        return queryBuilder.toString();
    }


    public String findTopSentimentsByAppNamesQuery(List<String> appNames) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?sentiment (COUNT(?sentiment) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appName {\n");

        // Append each appName from the list
        for (String appName : appNames) {
            queryBuilder.append("    \"" + appName + "\"\n");
        }

        queryBuilder.append("  }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:name ?appName;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:hasPart ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:CreativeWork;\n");
        queryBuilder.append("        schema:potentialAction ?potentialAction.\n");
        queryBuilder.append("  ?potentialAction rdf:type schema:ReactAction; \n");
        queryBuilder.append("                   schema:identifier ?sentiment.\n");
        queryBuilder.append("  FILTER (?sentiment IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?sentiment\n");

        return queryBuilder.toString();
    }

    public String findTopFeaturesByAppNamesQuery(List<String> appNames) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?feature (COUNT(?feature) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appName {\n");

        // Append each appName from the list
        for (String appName : appNames) {
            queryBuilder.append("    \"" + appName + "\"\n");
        }

        queryBuilder.append("  }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:name ?appName;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:hasPart ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:CreativeWork;\n");
        queryBuilder.append("        schema:keywords ?keyword.\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature.\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?feature\n");
        queryBuilder.append("ORDER BY DESC(?count)\n");
        queryBuilder.append("LIMIT 5\n");

        return queryBuilder.toString();
    }


}
