package upc.edu.gessi.repo.utils;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.gson.Gson;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.json.JSONObject;
import upc.edu.gessi.repo.domain.graph.Graph;
import upc.edu.gessi.repo.domain.graph.GraphEdge;
import upc.edu.gessi.repo.domain.graph.GraphNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils {

    public static TupleQueryResult runSparqlQuery(RepositoryConnection repositoryConnection, String query) {
        TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(query);
        return tupleQuery.evaluate();
    }

    public static String sanitizeString(String name) {
        String sanitizedName = name.replace(" ","_");
        sanitizedName = sanitizedName.replace("|","");
        sanitizedName = sanitizedName.replace("[","");
        sanitizedName = sanitizedName.replace("]","");
        return sanitizedName;
    }

    public static String escape(String text) {
        return text.replaceAll("[^ -~]+", "");
    }

    public static void saveJSONFile(Graph apps, String fileName) {
        FileWriter file = null;
        try {
            // Constructs a FileWriter given a file name, using the platform's default charset
            file = new FileWriter("src/main/resources/"+ fileName +".json");
            Gson gson = new Gson();
            String json = gson.toJson(apps);
            file.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    jsonGenerator.writeStartObject(); // {
     jsonGenerator.writeStringField("name", "India");
     jsonGenerator.writeNumberField("population", 10000);
     jsonGenerator.writeFieldName("listOfStates");
     jsonGenerator.writeStartArray();
     jsonGenerator.writeString("Madhya Pradesh");
     jsonGenerator.writeString("Maharashtra");
     jsonGenerator.writeString("Rajasthan");
     jsonGenerator.writeEndArray();
     jsonGenerator.writeEndObject();
     */

    private final String prefix = "https://schema.org/";

    public static void convertGraphSchemaToJSON(String sourceFile, String targetFile) {
        try {
            //Source file
            JsonParser parser = new JsonFactory().createParser(new File("src/main/resources/" + sourceFile));

            // Write json to a file
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(new File("src/main/resources/" + targetFile),
                    JsonEncoding.UTF8);
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

            //Start json object
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("nodes");
            jsonGenerator.writeStartObject();

            List<GraphEdge> edges = new ArrayList<>();

            parser.nextToken();                                         // START_OBJECT     '{'
            while (parser.nextToken() != null) {        // FIELD_NAME       'definedTerm'
                //String name = parser.getCurrentName();
                //parser.nextToken(); // JsonToken.START_ARRAY;
                //parser.nextValue();
                //New node
                String nodeKey = parser.getCurrentName();               // 'definedTerm'
                jsonGenerator.writeFieldName(nodeKey);
                jsonGenerator.writeStartObject();

                parser.nextToken();                                     // START_OBJECT     '{'
                parser.nextToken();                                     // FIELD_NAME       'name'

                String subNodeKey = parser.getCurrentName();            // 'name'
                parser.nextToken();                                     // START_ARRAY      '['

                while (parser.nextToken() != JsonToken.END_ARRAY) {                                     // START_OBJECT     '{'
                    extractProperty(parser, jsonGenerator, edges, nodeKey, subNodeKey);
                }

                //End node
                jsonGenerator.writeEndObject();
                parser.nextToken();                                         // END_OBJECT     '}'
            }

            jsonGenerator.writeEndObject();

            jsonGenerator.writeFieldName("edges");
            jsonGenerator.writeStartArray();
            //TODO edges
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
            parser.close();
            jsonGenerator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractProperty(JsonParser parser, JsonGenerator jsonGenerator, List<GraphEdge> edges, String nodeKey, String subNodeKey) throws IOException {
        //Write property name

        parser.nextToken();                                     // FIELD_NAME       'value'
        String key = parser.getCurrentName();                   // 'value'
        String value = parser.nextTextValue();                  // '10'
        parser.nextToken();                                     // FIELD_NAME       'type'
        String type = parser.nextTextValue();                   // 'type'
        jsonGenerator.writeStringField(subNodeKey, value);      // 'name': '10'

        if (type != null && type.equals("uri")) {
            //Add edge between nodeKey and subNodeKey
            edges.add(new GraphEdge(nodeKey, subNodeKey));
        }

        parser.nextToken();
        if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            parser.nextToken();
            parser.nextToken();     // '}'
        }

    }

    public static Graph getGraphFromJSON(JSONObject jsonObject) {
        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
        for (Iterator it = jsonObject.keys(); it.hasNext(); ) {
            String key = (String) it.next();
            //TODO

        }
        return new Graph(nodes, edges);
    }
}
