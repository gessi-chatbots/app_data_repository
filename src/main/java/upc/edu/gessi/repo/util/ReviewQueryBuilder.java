package upc.edu.gessi.repo.util;

import org.springframework.stereotype.Component;
import upc.edu.gessi.repo.dto.Review.ReviewDescriptorRequestDTO;

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

    public String findReviewsByIDsDescriptors(final List<String> reviewIds) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX schema: <https://schema.org/>
        PREFIX mapp: <https://gessi.upc.edu/en/tools/mapp-kg/>

        SELECT ?appId ?reviewId ?text 
               (GROUP_CONCAT(DISTINCT ?feature; SEPARATOR=", ") AS ?features) 
               (GROUP_CONCAT(DISTINCT ?emotion; SEPARATOR=", ") AS ?emotions) 
               (GROUP_CONCAT(DISTINCT ?model; SEPARATOR=", ") AS ?models) 
               (GROUP_CONCAT(DISTINCT ?polarityId; SEPARATOR=", ") AS ?polarities) 
               (GROUP_CONCAT(DISTINCT ?typeId; SEPARATOR=", ") AS ?types) 
               (GROUP_CONCAT(DISTINCT ?topicId; SEPARATOR=", ") AS ?topics)
        WHERE {
            ?app a schema:MobileApplication ;
                 schema:identifier ?appId ;
                 schema:review ?review .
            ?review a schema:Review ;
                    schema:identifier ?reviewId ;
                    schema:reviewBody ?text ;
                    schema:additionalProperty ?reviewSentence .
            ?reviewSentence a schema:Review ;
                            schema:keywords ?keywords .
            ?keywords a schema:DefinedTerm ;
                      schema:name ?feature ;
                      schema:disambiguatingDescription ?languageModel .
            ?languageModel a schema:softwareApplication ;
                           schema:identifier ?model .

            OPTIONAL {
                ?reviewSentence mapp:polarity ?polarity .
                ?polarity schema:identifier ?polarityId .
            }
            OPTIONAL {
                ?reviewSentence mapp:type ?type .
                ?type schema:identifier ?typeId .
            }
            OPTIONAL {
                ?reviewSentence mapp:topic ?topic .
                ?topic schema:identifier ?topicId .
            }
            OPTIONAL {
                ?reviewSentence schema:potentialAction ?potentialAction .
                ?potentialAction schema:identifier ?emotion .
            }
    """);

        if (reviewIds != null && !reviewIds.isEmpty()) {
            queryBuilder.append("  VALUES ?reviewId {\n");
            for (String reviewId : reviewIds) {
                queryBuilder.append("    \"").append(reviewId).append("\" \n");
            }
            queryBuilder.append("  }\n");
        }

        queryBuilder.append("""
        }
        GROUP BY ?appId ?reviewId ?text
    """);

        return queryBuilder.toString();
    }

    public String findReviewsIDsByDescriptors(final ReviewDescriptorRequestDTO requestDTO,
                                              final Integer page,
                                              final Integer size) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX schema: <https://schema.org/>
        PREFIX mapp: <https://gessi.upc.edu/en/tools/mapp-kg/>

        SELECT DISTINCT ?reviewId
        WHERE {
            ?app a schema:MobileApplication ;
                 schema:identifier ?appId ;
                 schema:review ?review .
            ?review a schema:Review ;
                    schema:identifier ?reviewId ;
                    schema:additionalProperty ?reviewSentence .
    """);

        // Extract values from requestDTO
        String appId = requestDTO.getAppId();
        String type = requestDTO.getType();
        String topic = requestDTO.getTopic();
        String polarity = requestDTO.getPolarity();
        String emotion = requestDTO.getEmotion();
        List<String> featureList = requestDTO.getFeatureList();

        if (appId != null && !appId.isEmpty()) {
            queryBuilder.append("  FILTER (?appId = \"").append(appId).append("\")\n");
        }

        if (type != null && !type.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence mapp:type ?typeCheck .\n")
                    .append("    ?typeCheck schema:identifier \"").append(type).append("\" .\n")
                    .append("  }\n");
        }

        if (topic != null && !topic.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence mapp:topic ?topicCheck .\n")
                    .append("    ?topicCheck schema:identifier \"").append(topic).append("\" .\n")
                    .append("  }\n");
        }

        if (polarity != null && !polarity.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence mapp:polarity ?polarityCheck .\n")
                    .append("    ?polarityCheck schema:identifier \"").append(polarity).append("\" .\n")
                    .append("  }\n");
        }

        if (emotion != null && !emotion.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence schema:potentialAction ?emotionCheck .\n")
                    .append("    ?emotionCheck schema:identifier \"").append(emotion).append("\" .\n")
                    .append("  }\n");
        }

        if (featureList != null && !featureList.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n");
            for (String feature : featureList) {
                queryBuilder.append("    ?reviewSentence schema:keywords ?featureCheck .\n")
                        .append("    ?featureCheck schema:name \"").append(feature).append("\" .\n");
            }
            queryBuilder.append("  }\n");
        }

        queryBuilder.append("}\n");

        if (size != null) {
            queryBuilder.append("LIMIT ").append(size).append("\n");
        }
        if (page != null && size != null) {
            int offset = page * size;
            queryBuilder.append("OFFSET ").append(offset).append("\n");
        }

        return queryBuilder.toString();
    }


    public String countByDescriptors(final ReviewDescriptorRequestDTO requestDTO) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX schema: <https://schema.org/>
        PREFIX mapp: <https://gessi.upc.edu/en/tools/mapp-kg/>

        SELECT (COUNT(DISTINCT ?reviewId) AS ?totalCount)
        WHERE {
            ?app a schema:MobileApplication ;
                 schema:identifier ?appId ;
                 schema:review ?review .
            ?review a schema:Review ;
                    schema:identifier ?reviewId ;
                    schema:additionalProperty ?reviewSentence .
    """);

        // Extract values from requestDTO
        String appId = requestDTO.getAppId();
        String type = requestDTO.getType();
        String topic = requestDTO.getTopic();
        String polarity = requestDTO.getPolarity();
        String emotion = requestDTO.getEmotion();
        List<String> featureList = requestDTO.getFeatureList();

        if (appId != null && !appId.isEmpty()) {
            queryBuilder.append("  FILTER (?appId = \"").append(appId).append("\")\n");
        }

        if (type != null && !type.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence mapp:type ?typeCheck .\n")
                    .append("    ?typeCheck schema:identifier \"").append(type).append("\" .\n")
                    .append("  }\n");
        }

        if (topic != null && !topic.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence mapp:topic ?topicCheck .\n")
                    .append("    ?topicCheck schema:identifier \"").append(topic).append("\" .\n")
                    .append("  }\n");
        }

        if (polarity != null && !polarity.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence mapp:polarity ?polarityCheck .\n")
                    .append("    ?polarityCheck schema:identifier \"").append(polarity).append("\" .\n")
                    .append("  }\n");
        }

        if (emotion != null && !emotion.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n")
                    .append("    ?reviewSentence schema:potentialAction ?emotionCheck .\n")
                    .append("    ?emotionCheck schema:identifier \"").append(emotion).append("\" .\n")
                    .append("  }\n");
        }

        if (featureList != null && !featureList.isEmpty()) {
            queryBuilder.append("  FILTER EXISTS {\n");
            for (String feature : featureList) {
                queryBuilder.append("    ?reviewSentence schema:keywords ?featureCheck .\n")
                        .append("    ?featureCheck schema:name \"").append(feature).append("\" .\n");
            }
            queryBuilder.append("  }\n");
        }

        queryBuilder.append("}\n");

        return queryBuilder.toString();
    }

}
