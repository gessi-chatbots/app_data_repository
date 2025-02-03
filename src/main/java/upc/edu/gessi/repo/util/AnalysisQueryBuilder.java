package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class AnalysisQueryBuilder
{

    public String findFeaturesByAppPackage(final String appPackage) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT DISTINCT ?feature\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appPackage { \"" + appPackage + "\" }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:identifier ?appPackage;\n");
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
    public String findEmotionsAndFeaturesStatisticBetweenDates(final String appPackage,
                                                               final Date startDate,
                                                               final Date endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\n");
        queryBuilder.append("SELECT ?emotion ?feature ?date\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?appPackage {\n");
        queryBuilder.append("    \"" + appPackage + "\"\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:identifier ?appPackage;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part ;\n");
        queryBuilder.append("          schema:datePublished ?date .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        schema:keywords ?keyword;\n");
        queryBuilder.append("        schema:additionalProperty ?potentialAction.\n");
        queryBuilder.append("  ?potentialAction rdf:type schema:ReactAction; \n");
        queryBuilder.append("                   schema:identifier ?emotion.\n");
        queryBuilder.append("  FILTER (?emotion IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature.\n\n");
        queryBuilder.append("  FILTER(?date >= \"" + dateFormat.format(startDate) + "\"^^xsd:dateTime &&\n");
        queryBuilder.append("         ?date <= \"" + dateFormat.format(endDate) + "\"^^xsd:dateTime)\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?emotion ?feature ?date\n");
        queryBuilder.append("ORDER BY ASC(?date)\n");

        return queryBuilder.toString();
    }
    public String findTopicStatisticBetweenDates(final String appName,
                                                          final Date startDate,
                                                          final Date endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\n");
        queryBuilder.append("SELECT ?emotion ?feature ?date\n");
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
        queryBuilder.append("                   schema:identifier ?emotion.\n");
        queryBuilder.append("  FILTER (?emotion IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature.\n\n");
        queryBuilder.append("  FILTER(?date >= \"" + dateFormat.format(startDate) + "\"^^xsd:dateTime &&\n");
        queryBuilder.append("         ?date <= \"" + dateFormat.format(endDate) + "\"^^xsd:dateTime)\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?emotion ?feature ?date\n");
        queryBuilder.append("ORDER BY ASC(?date)\n");

        return queryBuilder.toString();
    }
    public String findTypeStatisticBetweenDates(final String appName,
                                                        final Date startDate,
                                                        final Date endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\n");
        queryBuilder.append("SELECT ?emotion ?feature ?date\n");
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
        queryBuilder.append("                   schema:identifier ?emotion.\n");
        queryBuilder.append("  FILTER (?emotion IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature.\n\n");
        queryBuilder.append("  FILTER(?date >= \"" + dateFormat.format(startDate) + "\"^^xsd:dateTime &&\n");
        queryBuilder.append("         ?date <= \"" + dateFormat.format(endDate) + "\"^^xsd:dateTime)\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?emotion ?feature ?date\n");
        queryBuilder.append("ORDER BY ASC(?date)\n");

        return queryBuilder.toString();
    }
    public String findPolarityStatisticBetweenDates(final String appName,
                                                        final Date startDate,
                                                        final Date endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n\n");
        queryBuilder.append("SELECT ?emotion ?feature ?date\n");
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
        queryBuilder.append("                   schema:identifier ?emotion.\n");
        queryBuilder.append("  FILTER (?emotion IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature.\n\n");
        queryBuilder.append("  FILTER(?date >= \"" + dateFormat.format(startDate) + "\"^^xsd:dateTime &&\n");
        queryBuilder.append("         ?date <= \"" + dateFormat.format(endDate) + "\"^^xsd:dateTime)\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?emotion ?feature ?date\n");
        queryBuilder.append("ORDER BY ASC(?date)\n");

        return queryBuilder.toString();
    }
    public String findTopEmotionsByAppNamesQuery(List<String> appNames) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?emotion (COUNT(?emotion) AS ?count)\n");
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
        queryBuilder.append("                   schema:identifier ?emotion.\n");
        queryBuilder.append("  FILTER (?emotion IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?emotion\n");

        return queryBuilder.toString();
    }

    public String findTopEmotions() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?emotion (COUNT(?emotion) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        schema:additionalProperty ?potentialAction.\n");
        queryBuilder.append("  ?potentialAction rdf:type schema:ReactAction; \n");
        queryBuilder.append("                   schema:identifier ?emotion.\n");
        queryBuilder.append("  FILTER (?emotion IN (\"happiness\", \"sadness\", \"anger\", \"surprise\", \"fear\", \"disgust\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?emotion\n");
        return queryBuilder.toString();
    }

    public String findTopTypes() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX mapp: <https://gessi.upc.edu/en/tools/mapp-kg/>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?typeId (COUNT(?typeId) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        mapp:type ?type.\n");
        queryBuilder.append("  ?type rdf:type mapp:Type;\n");
        queryBuilder.append("          schema:identifier ?typeId.\n");
        queryBuilder.append("  FILTER (?typeId IN (\"Bug\", \"Rating\", \"Feature\", \"UserExperience\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?typeId\n");
        return queryBuilder.toString();
    }

    public String findTopPolarites() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX mapp: <https://gessi.upc.edu/en/tools/mapp-kg/>\n");
        queryBuilder.append("SELECT ?polarityId (COUNT(?polarityId) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        mapp:polarity ?polarity.\n");
        queryBuilder.append("  ?polarity rdf:type mapp:Polarity; \n");
        queryBuilder.append("                   schema:identifier ?polarityId.\n");
        queryBuilder.append("  FILTER (?polarityId IN (\"positive\", \"negative\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?polarityId\n");
        return queryBuilder.toString();
    }
    public String findTopTopics() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX mapp: <https://gessi.upc.edu/en/tools/mapp-kg/>\n");
        queryBuilder.append("SELECT ?topicId (COUNT(?topicId) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        mapp:topic ?topic.\n");
        queryBuilder.append("  ?topic rdf:type mapp:Topic;\n");
        queryBuilder.append("         schema:identifier ?topicId.\n");
        queryBuilder.append("  FILTER (?topicId IN (\"general\", \"usability\", \"effectiveness\", \"efficiency\", \"enjoyability\", \"cost\", \"reliability\", \"security\", \"compatibility\", \"learnability\", \"safety\", \"aesthetics\"))\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?topicId\n");
        return queryBuilder.toString();
    }

    public String findTopFeatures() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?feature (COUNT(?feature) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:name ?appName;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:additionalProperty ?part .\n");
        queryBuilder.append("  ?part rdf:type schema:Review;\n");
        queryBuilder.append("        schema:keywords ?keyword.\n");
        queryBuilder.append("  ?keyword rdf:type schema:DefinedTerm; \n");
        queryBuilder.append("           schema:identifier ?feature.\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?feature\n");
        queryBuilder.append("ORDER BY DESC(?count)\n");
        queryBuilder.append("LIMIT 50\n");
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
