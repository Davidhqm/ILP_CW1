package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class FlightPath {

    /**
     * An instance of the CoordinateSystem class used to call the methods belonged to the class.
     */
    private final CoordinateSystem coordinateHandler = new CoordinateSystem();

    /**
     * A PriorityQueue that stores weighted nodes. It prioritises the nodes with the lowest F value associated.
     */
    private PriorityQueue<WeightedLngLat> frontier;

    /**
     * A HashMap that stores a node as the key and its previous node as the value.
     */
    private HashMap<LngLat, LngLat> leadTo;

    /**
     * A HashMap that stores a node as the key and the cost reaching the node from the start as the value.
     */
    private HashMap<LngLat, Double> costSoFar;

    /**
     * A HashMap that stores a node as the key and its weighted counterpart as the value for easier look-ups and updates.
     */
    private HashMap<LngLat, WeightedLngLat> toCoLngLat;

    public FlightPath(){

    }

    /**
     * This is the pathfinding algorithm of the drone that models after the A* algorithm.
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
        //initialise the empty maps and queues
        this.frontier = new PriorityQueue<>();
        this.leadTo = new HashMap<>();
        this.costSoFar = new HashMap<>();
        this.toCoLngLat = new HashMap<>();

        double startDistance = coordinateHandler.distanceTo(start, goal); // the H value of start that implements Euler distance Heuristics
        WeightedLngLat coLngLat = new WeightedLngLat(start, startDistance); // the weighted version of start to be put in priorityQueue
        this.toCoLngLat.put(start, coLngLat); // map the original start to its weighted version
        this.frontier.add(coLngLat); // add the start to the priorityQueue
        this.leadTo.put(start, null); // no previous node before the start point
        this.costSoFar.put(start, (double) 0); // the cost to reach start is 0, the G value is 0
        ArrayList<LngLat> path = new ArrayList<>(); // the output ArrayList that stores the nodes from start to goal in order

        double newCost; // the G value(the cost to reach the current node) of a node
        double priority;// the F score which is G + H of a node
        LngLat currentNode;
        while (!frontier.isEmpty()) {
            currentNode = frontier.remove().getLngLat(); // get the one with the lowestCost in queue currently

            // if the current node is close to goal, traverse through leadTo and add the nodes defining to the path to the output ArrayList
            if (coordinateHandler.isCloseTo(currentNode, goal)) {
                LngLat nodeBack = currentNode;
                while (nodeBack != null) {
                    path.add(0, nodeBack);
                    nodeBack = leadTo.get(nodeBack);
                }
                break; // break out to return the path
            }

            for (LngLat neighbour : neighboursOf(currentNode, noFlyZones)){ // loop through the valid neighbours of the current node
                newCost = costSoFar.get(currentNode) + SystemConstants.DRONE_MOVE_DISTANCE;

                // handles the case when the drone is on the delivery mode, and it's current location is within central area
                if (isDelivery && coordinateHandler.isInRegion(currentNode, centralArea)){
                    if (coordinateHandler.isInRegion(neighbour, centralArea)) { // check if the neighbour stays in central area too
                        updateNeighbourPath(goal, newCost, currentNode, neighbour);
                    }
                }else{
                    updateNeighbourPath(goal, newCost, currentNode, neighbour);
                }
            }
        }
        return path;
    }

    /**
     * Updates the pathway to a neighbour if a faster route exists.
     * @param goal the goal of the path
     * @param newCost the cost to reach the neighbour
     * @param currentNode the current node under evaluation
     * @param neighbour the neighbour node to be updated
     */
    private void updateNeighbourPath(LngLat goal, double newCost, LngLat currentNode, LngLat neighbour) {
        double priority;
        if (!costSoFar.containsKey(neighbour)) { // if the neighbour has not been searched yet
            this.costSoFar.put(neighbour, newCost); // put the neighbour and its G value to the HashMap
            priority = newCost + coordinateHandler.distanceTo(neighbour, goal); // calculate its F value
            WeightedLngLat coLngLat = new WeightedLngLat(neighbour, priority);// get the weighted version of the neighbour
            this.toCoLngLat.put(neighbour, coLngLat); // map the original node with its weighted version
            this.frontier.add(coLngLat); // add the weighted node to the priorityQueue
            this.leadTo.put(neighbour, currentNode); // update the pathway to the neighbour
        } else if (newCost < costSoFar.get(neighbour)) { // if a shorter path to the neighbour emerges
            this.frontier.remove(toCoLngLat.get(neighbour)); // remove the old weighted neighbour
            this.costSoFar.replace(neighbour, newCost); // update the G value of the neighbour
            priority = newCost + coordinateHandler.distanceTo(neighbour, goal); // get the new F value
            WeightedLngLat coLngLat = new WeightedLngLat(neighbour, priority); // get the new weighted version of the neighbour
            this.frontier.add(coLngLat); // add the weighted node to the priorityQueue
            this.toCoLngLat.replace(neighbour, coLngLat); // update the mappings of the node and its weighted version
            this.leadTo.replace(neighbour, currentNode); // update the pathway to the neighbour
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
