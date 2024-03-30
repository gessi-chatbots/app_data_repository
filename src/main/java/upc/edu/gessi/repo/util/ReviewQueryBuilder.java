package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewQueryBuilder
{

    public String findTextReviewsQuery(final List<String> ids) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?app_identifier ?id ?text\n");
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
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:review ?review ;\n");
        queryBuilder.append("       schema:name ?app_identifier.\n");
        queryBuilder.append("}\n");
        queryBuilder.append("GROUP BY ?app_identifier ?id ?text");
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
}
