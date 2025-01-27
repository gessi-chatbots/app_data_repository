package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewQueryBuilder
{

    public String findAllQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?subject ?predicate ?object\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?subject rdf:type schema:Review .\n");
        queryBuilder.append("  ?subject ?predicate ?object .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }
    public String findAllQueryWithLimitOffset(int limit, int offset) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX sc: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?id ?text\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?subject rdf:type sc:Review ;\n");
        queryBuilder.append("           sc:reviewBody ?text ;\n");
        queryBuilder.append("           sc:datePublished ?date;\n");
        queryBuilder.append("           sc:identifier ?id .\n");
        queryBuilder.append("}\n");
        queryBuilder.append("ORDER BY DESC (?date)\n");
        queryBuilder.append("LIMIT ").append(limit).append("\n");
        queryBuilder.append("OFFSET ").append(offset).append("\n");
        return queryBuilder.toString();
    }

    public String findAllSimplifiedQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX sc: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?id ?text\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?subject rdf:type sc:Review ;\n");
        queryBuilder.append("           sc:reviewBody ?text ;\n");
        queryBuilder.append("           sc:identifier ?id .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String findReviewsByIds(final List<String> ids) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?app_identifier ?id ?text ?date\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?id {\n");

        for (String id : ids) {
            queryBuilder.append("    \"" + id + "\"");
            queryBuilder.append("\n");
        }

        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?id ;\n");
        queryBuilder.append("          schema:reviewBody ?text .\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?review schema:datePublished ?date .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:review ?review ;\n");
        queryBuilder.append("       schema:name ?app_identifier.\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?app_identifier ?id ?text ?date");

        return queryBuilder.toString();
    }




    public String findReviewSentencesEmotionsAux(final List<String> reviewIds) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?reviewId ?sentenceId ?sentimentId ?sentimentValue ?featureId ?featureValue\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?reviewId {\n");

        for (String reviewId : reviewIds) {
            queryBuilder.append("    \"" + reviewId + "\"\n");
        }

        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?reviewId ;\n");
        queryBuilder.append("          schema:reviewBody ?reviewBody .\n");
        queryBuilder.append("  FILTER (!isLiteral(?reviewBody))\n");
        queryBuilder.append("  ?reviewBody schema:identifier ?sentenceId .\n");
        queryBuilder.append("  \n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewBody schema:ReactAction ?sentimentIDObject .\n");
        queryBuilder.append("    ?sentimentIDObject schema:identifier ?sentimentId ;\n");
        queryBuilder.append("                     schema:ReactAction ?sentimentValue .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewBody schema:keywords ?featureSentenceObject .\n");
        queryBuilder.append("    ?featureSentenceObject schema:identifier ?featureId ;\n");
        queryBuilder.append("                           schema:keywords ?featureValue .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");

        return queryBuilder.toString();
    }

    public String findReviewSentencesEmotions(final List<String> reviewIds) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?reviewId ?sentenceId ?sentimentValue ?featureValue\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?reviewId {\n");
        for (String reviewId : reviewIds) {
            queryBuilder.append("    \"" + reviewId + "\"\n");
        }
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?reviewId ;\n");
        queryBuilder.append("          schema:hasPart ?reviewPart .\n");
        queryBuilder.append("  ?reviewPart schema:identifier ?sentenceId .\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewPart schema:potentialAction ?sentiment .\n");
        queryBuilder.append("    ?sentiment schema:identifier ?sentimentValue .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewPart schema:keywords ?feature .\n");
        queryBuilder.append("    ?feature schema:identifier ?featureValue .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String hasReviewSentiments(String reviewId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT (IF((COUNT(?sentiment) > 0), \"true\", \"false\") as ?hasSentiment)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?reviewId {\n");
        queryBuilder.append("    \"" + reviewId + "\"\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?reviewId ;\n");
        queryBuilder.append("          schema:hasPart ?reviewPart .\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewPart schema:potentialAction ?sentiment .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?review\n");
        return queryBuilder.toString();
    }

    public String hasReviewFeatures(String reviewId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT (IF((COUNT(?keyword) > 0), \"true\", \"false\") as ?hasKeyword)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?reviewId {\n");
        queryBuilder.append("    \"" + reviewId + "\"\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?reviewId ;\n");
        queryBuilder.append("          schema:hasPart ?reviewPart .\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewPart schema:keywords ?keyword .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?review\n");
        return queryBuilder.toString();
    }
    public String deleteSentimentsFromReview(String reviewId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("DELETE {\n");
        queryBuilder.append("  ?reviewPart schema:potentialAction ?sentiment .\n");
        queryBuilder.append("}\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?reviewId {\n");
        queryBuilder.append("    \"" + reviewId + "\"\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?reviewId ;\n");
        queryBuilder.append("          schema:hasPart ?reviewPart .\n");
        queryBuilder.append("  ?reviewPart schema:potentialAction ?sentiment .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String deleteFeaturesFromReview(String reviewId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("DELETE {\n");
        queryBuilder.append("  ?reviewPart schema:keywords ?feature .\n");
        queryBuilder.append("}\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?reviewId {\n");
        queryBuilder.append("    \"" + reviewId + "\"\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?reviewId ;\n");
        queryBuilder.append("          schema:hasPart ?reviewPart .\n");
        queryBuilder.append("  ?reviewPart schema:keywords ?feature .\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String deleteByIDQuery(final String reviewId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("DELETE\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?s schema:identifier \"").append(reviewId).append("\" .\n");
        queryBuilder.append("  ?s ?p ?o .\n");
        queryBuilder.append("  ?app schema:review ?s .\n");
        queryBuilder.append("}");
        return queryBuilder.toString();
    }
    public String findReviewsByAppNameAndIdentifierWithLimitQuery(
            final String appName,
            final String appIdentifier,
            final int size) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT ?id ?author ?date ?reviewText\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?subject rdf:type schema:MobileApplication ;\n");
        queryBuilder.append("           schema:identifier \"").append(appIdentifier).append("\" ;\n");
        queryBuilder.append("           schema:name \"").append(appName).append("\" ;\n");
        queryBuilder.append("           schema:review ?review .\n");
        queryBuilder.append("  ?review rdf:type schema:Review ;\n");
        queryBuilder.append("          schema:identifier ?id ;\n");
        queryBuilder.append("          schema:author ?author ;\n");
        queryBuilder.append("          schema:datePublished ?date ;\n");
        queryBuilder.append("          schema:reviewBody ?reviewText .\n");
        queryBuilder.append("}\n");
        queryBuilder.append("ORDER BY ASC(?date)\n");
        queryBuilder.append("LIMIT ").append(size).append("\n");
        return queryBuilder.toString();
    }

    public String getCountQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX sc: <https://schema.org/>\n");
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("SELECT (COUNT(?subject) AS ?count)\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("    ?subject rdf:type sc:Review .\n");
        queryBuilder.append("}");
        return queryBuilder.toString();
    }

    public String findReviewsByAppIdAndFeatures(String appId, final List<String> features) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("PREFIX mapp: <https://gessi.upc.edu/en/tools/mapp-kg/>\n");
        queryBuilder.append("SELECT ?id ?text ?feature ?model ?polarityId ?typeId ?topicId\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  ?app a schema:MobileApplication ;\n");
        queryBuilder.append("       schema:identifier \"" + appId + "\" ;\n");
        queryBuilder.append("       schema:review ?review .\n");
        queryBuilder.append("  ?review a schema:Review ;\n");
        queryBuilder.append("         schema:identifier ?id;\n");
        queryBuilder.append("         schema:reviewBody ?text;\n");
        queryBuilder.append("         schema:additionalProperty ?reviewSentence .\n");
        queryBuilder.append("  ?reviewSentence a schema:Review; \n");
        queryBuilder.append("                  schema:keywords ?keywords .\n");
        queryBuilder.append("  ?keywords a schema:DefinedTerm ;\n");
        queryBuilder.append("            schema:name ?feature ;\n");
        queryBuilder.append("            schema:disambiguatingDescription ?languageModel .\n");
        queryBuilder.append("  ?languageModel a schema:softwareApplication ;\n");
        queryBuilder.append("                 schema:identifier ?model .\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewSentence mapp:polarity ?polarity .\n");
        queryBuilder.append("    ?polarity schema:identifier ?polarityId .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewSentence mapp:type ?type .\n");
        queryBuilder.append("    ?type schema:identifier ?typeId .\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("  OPTIONAL {\n");
        queryBuilder.append("    ?reviewSentence mapp:topic ?topic .\n");
        queryBuilder.append("    ?topic schema:identifier ?topicId .\n");
        queryBuilder.append("  }\n");
        if (features != null && !features.isEmpty()) {
            queryBuilder.append("  VALUES ?feature {\n");
            for (String feature : features) {
                queryBuilder.append("    \"" + feature + "\"\n");
            }
            queryBuilder.append("  }\n");
        }
        queryBuilder.append("}\n");

        return queryBuilder.toString();
    }





}
