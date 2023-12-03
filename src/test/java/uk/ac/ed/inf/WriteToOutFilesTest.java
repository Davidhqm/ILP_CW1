package uk.ac.ed.inf;

import org.json.JSONObject;
import org.junit.Test;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class WriteToOutFilesTest {

    @Test
    public void testOutput() throws IOException {
        URL urlOrders = new URL("https://ilp-rest.azurewebsites.net/orders/2023-09-03");
        URL urlRestaurants = new URL("https://ilp-rest.azurewebsites.net/restaurants");
        URL urlCentralArea = new URL("https://ilp-rest.azurewebsites.net/centralArea");
        URL urlNoFlyZones = new URL("https://ilp-rest.azurewebsites.net/noFlyZones");
        NamedRegion central = JsonController.fromJsonAll(urlCentralArea, NamedRegion.class);
        NamedRegion[] noFlyZones = JsonController.fromJsonAllRegions(urlNoFlyZones);
        Order[] orders = JsonController.fromJsonAllOrders(urlOrders);
        Restaurant[] restaurants = JsonController.fromJsonAllRestaurants(urlRestaurants);
        LngLat appletonTower = new LngLat(-3.186874, 55.944494);
        OrderValidator orderValidator = new OrderValidator();

        ArrayList<DroneMovement> droneMovements = new ArrayList<>();
        ArrayList<OrderOutline> orderOutlines = new ArrayList<>();
        ArrayList<Order> validOrders = orderValidator.filterAllValidOn(orders, restaurants, orderOutlines);
        JSONObject featureCollection = JsonController.generateLineStringJson();

        IOhandler.getAllPaths(validOrders, restaurants, appletonTower, noFlyZones, central, featureCollection, droneMovements);
        IOhandler.writeOutFiles(orderOutlines, droneMovements, featureCollection,
                "deliveryTest0903.json", "flightpathTest0903.json", "droneTest0903.geojson");



    }
}