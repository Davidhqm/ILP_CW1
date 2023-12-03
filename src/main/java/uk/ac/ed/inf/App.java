package uk.ac.ed.inf;

import org.json.JSONObject;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.data.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final String RESTAURANT_URL = "restaurants";
    public static final String Order_URL = "orders/";
    public static final String NoFlyZone = "noFlyZones";
    public static final String CentralArea = "centralArea";
    public static final LngLat appletonTower = new LngLat(-3.186874, 55.944494);
    public static final String JSON_SUFFIX = ".json";
    public static final String GEOJSON_SUFFIX = ".geojson";

    public static void main(String[] args) throws IOException {
        if (args.length < 1){
            System.err.println("the base URL must be provided");
            System.exit(1);
        }

        String date = args[0];
        String baseUrl = args[1];
        if (!baseUrl.endsWith("/")){
            baseUrl += "/";
        }

        try {
            URL serverUrl = new URL(baseUrl);
        } catch (Exception x) {
            System.err.println("The URL is invalid: " + x);
            System.exit(2);
        }

        // initialise and retrieve all relevant information from the server
        Restaurant[] restaurants = JsonController.fromJsonAllRestaurants((new URL(baseUrl + RESTAURANT_URL)));
        Order[] orders = JsonController.fromJsonAllOrders(new URL(baseUrl+Order_URL+date));
        NamedRegion[] noFlyZones = JsonController.fromJsonAllRegions(new URL(baseUrl+NoFlyZone));
        NamedRegion central = JsonController.fromJsonAll(new URL(baseUrl+CentralArea), NamedRegion.class);

        OrderValidator orderValidator = new OrderValidator();

        // initialise the output data carriers
        ArrayList<DroneMovement> droneMovements = new ArrayList<>();
        ArrayList<OrderOutline> orderOutlines = new ArrayList<>();
        JSONObject featureCollection = JsonController.generateLineStringJson();

        // Populate orderOutlines and return a list of valid orders
        ArrayList<Order> validOrders = orderValidator.filterAllValidOn(orders, restaurants, orderOutlines);

        // Populate droneMovements and featureCollection
        IOhandler.getAllPaths(validOrders, restaurants, appletonTower, noFlyZones, central, featureCollection, droneMovements);

        // write the data carriers to the three output files
        String deliveriesFileName = "deliveries-" + date + JSON_SUFFIX;
        String flightpathFileName = "flightpath-" + date + JSON_SUFFIX;
        String droneFileName = "drone-" + date + GEOJSON_SUFFIX;
        IOhandler.writeOutFiles(orderOutlines, droneMovements, featureCollection,
                                deliveriesFileName, flightpathFileName, droneFileName);
    }
}
