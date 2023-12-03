package uk.ac.ed.inf;

import org.json.JSONObject;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.util.ArrayList;

public class IOhandler {

    private static final FlightPath flightPath = new FlightPath();

    private static final CoordinateSystem coordinateHandler = new CoordinateSystem();

    private static final OrderValidator orderValidator = new OrderValidator();

    /**
     * Write the three output files under the resultfiles directory.
     * @param orderOutlines an ArrayList of orderOutline java records
     * @param droneMovements an ArrayList of DroneMovement java records
     * @param featureCollection a JSONObject that has one feature of LineString type
     * @param orderOutlineFileName file name of deliveries-YYYY-MM-DD.json
     * @param droneMovementsFileName file name of flightpath-YYYY-MM-DD.json
     * @param featureCollectionFileName file name of drone-YYYY-MM-DD.geojson
     */
    public static void writeOutFiles(ArrayList<OrderOutline> orderOutlines, ArrayList<DroneMovement> droneMovements, JSONObject featureCollection,
                                     String orderOutlineFileName, String droneMovementsFileName, String featureCollectionFileName){

        JsonController.writeToJsonOrderOutline(orderOutlines, orderOutlineFileName);
        JsonController.writeToJsonFile(droneMovements, droneMovementsFileName);
        JsonController.convertToGeoJSON(featureCollection, featureCollectionFileName);
    }


    /**
     * Populate the JSONObject featureCollection and the ArrayList droneMovements with proper records with all the valid orders of a day
     * featureCollection should contain all the nodes of the flightpath of the drone in a day
     * droneMovements should contain all the records of the drone movement in a day
     * @param validOrders an ArrayList of all valid orders of a day
     * @param restaurants an array of restaurants retrieved from server
     * @param appletonTower the LngLat coordinate of appletonTower
     * @param noFlyZones an array of noFlyZones retrieved from server
     * @param centralArea the central area region to bound the movement of the drone
     * @param featureCollection the JSONObject that a LineString feature to illustrate the flightPath of the drone
     * @param droneMovements an ArrayList of DroneMovement records to document to the behaviours of the drone
     */
    public static void getAllPaths(ArrayList<Order> validOrders, Restaurant[] restaurants, LngLat appletonTower, NamedRegion[] noFlyZones,
                                   NamedRegion centralArea, JSONObject featureCollection, ArrayList<DroneMovement> droneMovements){

        ArrayList<ArrayList<LngLat>> currentPaths = new ArrayList<>();
        Restaurant currentRestaurant;
        String currentOrderNum;
        for (int i = 0; i < validOrders.size(); i++) {
            currentRestaurant = orderValidator.getOrderRestaurant(validOrders.get(i), restaurants);
            currentOrderNum = validOrders.get(i).getOrderNo();
            if (i == 0){
                // the first order starts from appletonTower strictly and update the currentPaths
                currentPaths = retrieveAndBack(appletonTower, currentRestaurant.location(), appletonTower, noFlyZones, centralArea);

                // write the information to droneMovements and featureCollection
                recordDroneMovement(droneMovements, currentOrderNum, currentPaths);
                JsonController.addNodes(currentPaths.get(0),featureCollection);
                JsonController.addNodes(currentPaths.get(1).subList(1,currentPaths.get(1).size()), featureCollection);
            }else {
                // other order starts from where the drone was last hovered (a LngLat near appleton)
                LngLat pseudoStart = currentPaths.get(1).get(currentPaths.get(1).size() -1);

                // update the currentPaths and write the information to droneMovements and featureCollection
                currentPaths = retrieveAndBack(pseudoStart, currentRestaurant.location(), appletonTower, noFlyZones, centralArea);
                recordDroneMovement(droneMovements, currentOrderNum, currentPaths);
                JsonController.addNodes(currentPaths.get(0).subList(1, currentPaths.get(0).size()), featureCollection);
                JsonController.addNodes(currentPaths.get(1).subList(1, currentPaths.get(1).size()), featureCollection);
            }
        }
    }

    /**
     * Get an output ArrayList containing two ArrayLists that represent the two paths every order must take to deliver the pizzas.
     * The first path in the output ArrayList is the path from (near)AppletonTower to the restaurant of the order.
     * The second path in the output ArrayList is the path from (near)the restaurant of the order back to AppletonTower.
     * @param pseudoStart a approximate start coordinate that represents either from near the restaurant or near appletonTower
     * @param restaurant the coordinate of the restaurant
     * @param appletonTower the coordinate of appletonTower
     * @param noFlyZones an array of noFlyZones
     * @param centralArea central area region
     * @return an output ArrayList containing two ArrayLists of LngLat type
     */
    public static ArrayList<ArrayList<LngLat>> retrieveAndBack(LngLat pseudoStart, LngLat restaurant, LngLat appletonTower,
                                                       NamedRegion[] noFlyZones, NamedRegion centralArea){

        // create an arraylist that contains two arraylists, one is the path from appletonTower to the restaurants,
        // the other is a path from the restaurant back to appletonTower.
        ArrayList<ArrayList<LngLat>> toAndBack = new ArrayList<>();
        ArrayList<LngLat> toRestaurant = flightPath.searchAlgo(pseudoStart, restaurant, noFlyZones, centralArea, false);
        ArrayList<LngLat> backToAppleton = flightPath.searchAlgo(toRestaurant.get(toRestaurant.size()-1), appletonTower, noFlyZones,
                centralArea, true);
        toAndBack.add(toRestaurant);
        toAndBack.add(backToAppleton);
        return toAndBack;
    }

    /**
     * Document the all movements of the Drone to deliver an order successfully in an Arraylist of DroneMovement records.
     * The Drone takes off from appletonTower and flies to the restaurant, hovers for one move,
     * then fly back to appletonTower and hovers for one move.
     * @param droneMovements the ArrayList containing every single DroneMovement record the drone takes
     * @param orderNumber the associated order number of a move
     * @param toAndBack the two paths of an order(from appletonTower to restaurant and back)
     */
    public static void recordDroneMovement(ArrayList<DroneMovement> droneMovements, String orderNumber,
                                            ArrayList<ArrayList<LngLat>> toAndBack){
        ArrayList<LngLat> toRestaurant = toAndBack.get(0);
        ArrayList<LngLat> backToAppleton = toAndBack.get(1);
        addThisOrderMovements(droneMovements, orderNumber, toRestaurant);
        addThisOrderMovements(droneMovements, orderNumber, backToAppleton);
    }

    /**
     * Document a path of nodes associated with a certain order into an Arraylist of DroneMovement records.
     * The documented path is either from appletonTower to a restaurant or reversed.
     * @param droneMovements an ArrayList of DroneMovement type
     * @param orderNumber the order number of that associates with the path
     * @param path the path to be documented
     */
    private static void addThisOrderMovements(ArrayList<DroneMovement> droneMovements, String orderNumber, ArrayList<LngLat> path) {
        DroneMovement currentMove;
        double angle;
        for(int i = 0; i < path.size(); i++){
            if(i == path.size()-1){
                currentMove = new DroneMovement(orderNumber, path.get(i).lng(), path.get(i).lat(), 999,
                        path.get(i).lng(), path.get(i).lat());
                droneMovements.add(currentMove);
            }
            else {
                angle = coordinateHandler.getAngle(path.get(i), path.get(i + 1));
                currentMove = new DroneMovement(orderNumber, path.get(i).lng(), path.get(i).lat(), angle,
                        path.get(i + 1).lng(), path.get(i + 1).lat());
                droneMovements.add(currentMove);
            }
        }
    }
}
