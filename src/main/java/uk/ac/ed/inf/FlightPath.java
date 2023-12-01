package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class FlightPath {

    private PriorityQueue<WeightedLngLat> frontier = new PriorityQueue<>();
    private final CoordinateSystem coordinateHandler = new CoordinateSystem();
    private HashMap<LngLat, LngLat> leadTo = new HashMap<>();
    private HashMap<LngLat, Double> costSoFar = new HashMap<>();
    private HashMap<LngLat, WeightedLngLat> toCoLngLat = new HashMap<>();

    public FlightPath(){

    }

    /**
     * This is the pathfinding algorithm of the drone.
     * The movement behaviours of the drone should strictly follow as stated below.
     * <p>
     *     The drone is either flying from Appleton tower to pick up pizzas or flying from a restaurant to deliver pizzas.
     *     When the drone is on delivery mode(@param isDelivery is true), once entered the central area region(@param centralArea),
     *     it should not leave the area until the pizzas are delivered.
     * </p>
     * The algorithm should return a list of nodes documenting the movement of the drone.
     * @param start the starting coordinate
     * @param goal the destination coordinate
     * @param noFlyZones an array of noFlyZones
     * @param centralArea centralArea region to bound the behaviour of the drone delivery mode
     * @param isDelivery a boolean type determinant to check if the drone is to pick up pizzas or to deliver pizzas
     * @return an ArrayList of LngLat coordinates in order to link the path
     */
    public ArrayList<LngLat> searchAlgo(LngLat start, LngLat goal, NamedRegion[] noFlyZones,NamedRegion centralArea, Boolean isDelivery) {
        double startDistance = coordinateHandler.distanceTo(start, goal);
        WeightedLngLat coLngLat = new WeightedLngLat(start, startDistance);
        this.toCoLngLat.put(start, coLngLat);
        this.frontier.add(coLngLat);
        this.leadTo.put(start, null);
        this.costSoFar.put(start, (double) 0);
        ArrayList<LngLat> path = new ArrayList<>();
        double newCost;
        double priority;
        LngLat currentNode;
        while (!frontier.isEmpty()) {
            currentNode = frontier.remove().getLngLat(); //get the one with the lowestCost in queue currently

            if (coordinateHandler.isCloseTo(currentNode, goal)) {
                LngLat nodeBack = currentNode;
                while (nodeBack != null) {
                    path.add(0, nodeBack);
                    nodeBack = this.leadTo.get(nodeBack);
                }
                break;
            }

            for (LngLat neighbour : neighboursOf(currentNode, noFlyZones)) {
                if (leadTo.get(currentNode) == null){
                    newCost = costSoFar.get(currentNode) + SystemConstants.DRONE_MOVE_DISTANCE;
                    this.costSoFar.put(neighbour, newCost);
                    priority = newCost + coordinateHandler.distanceTo(neighbour, goal);
                    coLngLat = new WeightedLngLat(neighbour, priority);
                    this.toCoLngLat.put(neighbour, coLngLat);
                    this.frontier.add(coLngLat);
                    this.leadTo.put(neighbour, currentNode);
                }
                else {
                    newCost = costSoFar.get(currentNode) + SystemConstants.DRONE_MOVE_DISTANCE;
                    if (isDelivery && coordinateHandler.isInRegion(currentNode, centralArea)){
                        if (coordinateHandler.isInRegion(neighbour, centralArea)) {
                            updateNeighbourPath(goal, newCost, currentNode, neighbour);
                        }
                    }else {
                        updateNeighbourPath(goal, newCost, currentNode, neighbour);
                    }
                }
            }
        }
        return path;
    }

    private void updateNeighbourPath(LngLat goal, double newCost, LngLat currentNode, LngLat neighbour) {
        double priority;
        if (!costSoFar.containsKey(neighbour)) {
            this.costSoFar.put(neighbour, newCost);
            priority = newCost + coordinateHandler.distanceTo(neighbour, goal);
            WeightedLngLat coLngLat = new WeightedLngLat(neighbour, priority);
            this.toCoLngLat.put(neighbour, coLngLat);
            this.frontier.add(coLngLat);
            this.leadTo.put(neighbour, currentNode);
        } else if (newCost < costSoFar.get(neighbour)) {
            this.frontier.remove(toCoLngLat.get(neighbour));
            this.costSoFar.replace(neighbour, newCost);
            priority = newCost + coordinateHandler.distanceTo(neighbour, goal);
            WeightedLngLat coLngLat = new WeightedLngLat(neighbour, priority);
            this.frontier.add(coLngLat);
            this.toCoLngLat.replace(neighbour, coLngLat);
            this.leadTo.replace(neighbour, currentNode);
        }
    }

    /**
     * Output a list of valid neighbours from the next 16 possible positions of the current node.
     * A valid neighbouring node should not be inside any noFlyZones.
     * @param node the current position of the drone
     * @param noFlyZones an array of noFlyZones retrieved from server
     * @return a list of valid neighbouring points
     */
    public ArrayList<LngLat> neighboursOf(LngLat node, NamedRegion[] noFlyZones){
        ArrayList<LngLat> output = new ArrayList<>();
        LngLat nextTestNode;
        int counter;
        for(int i = 0; i < 16; i++){ // loop through all the 16 possible neighbours (16)
            counter = 0;
            nextTestNode = coordinateHandler.nextPosition(node, i * 22.5); //(22.5)
            for (NamedRegion noFlyZone : noFlyZones) { // check if the node is in a noFlyZone
                if (!coordinateHandler.isInRegion(nextTestNode, noFlyZone)) {
                    counter++;
                }
            }
            if (counter == noFlyZones.length){ //if a valid neighbour(not in any noFlyZone, add to neighbour list)
                output.add(nextTestNode);
            }
        }
        return output;
    }
}
