package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class JsonController {

    private static final ObjectMapper objectMapper = getDefaultObjectMapper();

    private static ObjectMapper getDefaultObjectMapper(){
        ObjectMapper defaultObjectMapper = new ObjectMapper();
        defaultObjectMapper.registerModule(new JavaTimeModule());
        return defaultObjectMapper;
    }

    public static JsonNode parse(String jsonString) throws JsonProcessingException {
        return objectMapper.readTree(jsonString);
    }

    /**
     * Deserialize a Json string provided by an url to return an instance of the targeted class.
     * @param url the input url
     * @param target the output target class
     * @return an instance of the target class
     * @param <T> a generic output class
     * @throws IOException all
     */
    public static <T> T fromJsonAll (URL url, Class<T> target) throws IOException {
        return objectMapper.readValue(url, target);
    }

    /**
     * Deserialize a Json string provided by an url to return an array of orders.
     * @param url the input url
     * @return an array of orders from server
     * @throws IOException all
     */
    public static Order[] fromJsonAllOrders (URL url) throws IOException {
        return objectMapper.readValue(url, new TypeReference<>() {});
    }

    /**
     * Deserialize a Json string provided by an url to return an array of restaurants.
     * @param url the input url
     * @return an array of restaurants from server
     * @throws IOException all
     */
    public static Restaurant[] fromJsonAllRestaurants (URL url) throws IOException {
        return objectMapper.readValue(url, new TypeReference<>() {});
    }

    /**
     * Deserialize a Json string provided by an url to return an array of regions.
     * @param url the input url
     * @return an array of regions from server
     * @throws IOException all
     */
    public static NamedRegion[] fromJsonAllRegions (URL url) throws IOException {
        return objectMapper.readValue(url, new TypeReference<>() {});
    }

    /**
     * Generate a JSONObject FeatureCollection with only one feature of type LineString.
     * @return the JSONObject FeatureCollection
     */
    public static JSONObject generateLineStringJson(){
        JSONObject featureCollection = new JSONObject();
        JSONArray features = new JSONArray();

        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");

        JSONObject geometry = new JSONObject();
        geometry.put("type", "LineString");
        geometry.put("coordinates", new JSONArray());

        feature.put("geometry", geometry);
        feature.put("properties", new JSONObject());

        features.put(feature);
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", features);
        return featureCollection;

    }

    /**
     * Add a list of nodes to the coordinates array of the LineString feature.
     * @param lngLats an ArrayList containing the nodes defining a path
     * @param featureCollection the JSONObject that contains the LineString feature
     */
    public static void addNodes(List<LngLat> lngLats, JSONObject featureCollection){
        if (featureCollection.has("features")) {
            JSONArray features = (JSONArray) featureCollection.get("features");
            if (!features.isEmpty()) {
                JSONObject feature = (JSONObject) features.get(0); // Assuming only one feature
                if (feature.has("geometry")) {
                    JSONObject geometry = (JSONObject) feature.get("geometry");
                    if (geometry.has("coordinates")) {
                        JSONArray coordinates = (JSONArray) geometry.get("coordinates");

                        // Add points to the coordinates array
                        for (LngLat lngLat : lngLats) {
                            JSONArray coord = new JSONArray();
                            coord.put(lngLat.lng());
                            coord.put(lngLat.lat());
                            coordinates.put(coord);
                        }
                    }
                }
            }
        }
    }

    /**
     * Write a featureCollection with one feature of LineString type to an output file.
     * @param object a JSONObject featureCollection
     * @param fileName the name of the output file
     */
    public static void convertToGeoJSON(JSONObject object, String fileName) {

        String filePath = "resultfiles/" + fileName;
        File directory = new File("resultfiles");
        if (!directory.exists()) {
            directory.mkdir();
        }

        File output = new File(filePath);
        if(output.exists()){
            output.delete();
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            // Write the JSON content to the file
            fileWriter.write(object.toString());
            System.out.println("GeoJSON written to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a list of DroneMovement records to an output file containing an array of JSON records.
     * @param records a list of DroneMovement records
     * @param fileName the name of the output file
     */
    public static void writeToJsonFile(ArrayList<DroneMovement> records, String fileName) {

        try {
            // Convert records to JSON
            String json = objectMapper.writeValueAsString(records);

            // Specify the file path
            String filePath = "resultfiles/" + fileName;

            // Create the "resultfiles" directory if it doesn't exist
            File directory = new File("resultfiles");
            if (!directory.exists()) {
                directory.mkdir();
            }

            File output = new File(filePath);
            if(output.exists()){
                output.delete();
            }

            // Write JSON to file
            objectMapper.writeValue(new File(filePath), records);
            System.out.println("Records written to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a list of OrderOutline records to an output file containing an array of JSON records.
     * @param orderOutlines a list of OrderOutline records
     * @param fileName the name of the output file
     */
    public static void writeToJsonOrderOutline(ArrayList<OrderOutline> orderOutlines, String fileName) {

        try {
            // Specify the file path
            String filePath = "resultfiles/" + fileName;

            // Create the "resultfiles" directory if it doesn't exist
            File directory = new File("resultfiles");
            if (!directory.exists()) {
                directory.mkdir();
            }

            File output = new File(filePath);
            if(output.exists()){
                output.delete();
            }

            // Write JSON to file
            objectMapper.writeValue(new File(filePath), orderOutlines);
            System.out.println("Records written to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
