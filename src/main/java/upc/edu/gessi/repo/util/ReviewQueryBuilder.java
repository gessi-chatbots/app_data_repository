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
            queryBuilder.append("    \"" + id + "\"\n");
        }
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?id ;\n");
        queryBuilder.append("          schema:reviewBody ?text .\n");
        queryBuilder.append("  ?app rdf:type schema:MobileApplication;\n");
        queryBuilder.append("       schema:review ?review ;\n");
        queryBuilder.append("       schema:identifier ?app_identifier\n");
        queryBuilder.append("  FILTER(isLiteral(?text))\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

    public String findReviewSentencesEmotions(final List<String> ids) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        queryBuilder.append("PREFIX schema: <https://schema.org/>\n");
        queryBuilder.append("SELECT ?review_id ?sentence_id ?emotion\n");
        queryBuilder.append("WHERE {\n");
        queryBuilder.append("  VALUES ?review_id {\n");
        for (String id : ids) {
            queryBuilder.append("    \"" + id + "\"\n");
        }
        queryBuilder.append("  }\n");
        queryBuilder.append("  ?review rdf:type schema:Review;\n");
        queryBuilder.append("          schema:identifier ?review_id ;\n");
        queryBuilder.append("          schema:reviewBody ?review_text .\n");
        queryBuilder.append("  ?review_text schema:identifier ?sentence_id;\n");
        queryBuilder.append("               schema:ReactAction ?emotion\n");
        queryBuilder.append("}\n");
        return queryBuilder.toString();
    }

}
