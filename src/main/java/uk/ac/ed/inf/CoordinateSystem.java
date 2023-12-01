package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

public class CoordinateSystem implements LngLatHandling {

    public CoordinateSystem(){}

    /**
     * Return the Euler distance between two coordinates.
     * @param startPosition the starting position
     * @param endPosition the ending position
     * @return the Euler distance between two coordinates
     */
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        return Math.sqrt(Math.pow(startPosition.lng() - endPosition.lng(), 2) +
                Math.pow(startPosition.lat() - endPosition.lat(), 2));
    }

    /**
     * Check if the two points are closer than 1.5E-4.
     * @param startPosition the first test point
     * @param otherPosition the second test point
     * @return the boolean value of the result
     */
    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    /**
     * Check if a given point is inside a defined region.
     * Cast a ray to every edge of the region and count the crossing numbers.
     * Odd crossings indicates the test point lies inside the region, otherwise outside.
     * @param position the test point
     * @param region the defined polygonal region
     * @return true if the test point is inside the region, else false
     */
    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        int crossingCounter = 0;
        LngLat v1;
        LngLat v2;
        for (int i = 0; i < region.vertices().length; i++ ){
            v1 = region.vertices()[i];
            if (i == region.vertices().length - 1){ // if v1 is the last node in the array, v1 is connected by the first node in the array
                v2 = region.vertices()[0];
            }
            else{
                v2 = region.vertices()[i+1];
            }

            // cast a ray to the right side from the test point.
            // check if the y of the test point lies between the range,
            // if not then the ray cast won't cross the edge
            if ( ((position.lat()<v1.lat()) != (position.lat()<v2.lat()))){
                if(position.lng() == v1.lng() + ((position.lat()-v1.lat())/(v2.lat()- v1.lat()))*(v2.lng()- v1.lng())){
                    return true; // if the test point lies on the edge, it is in region
                }
                if (position.lng() < v1.lng() + ((position.lat()-v1.lat())/(v2.lat()- v1.lat()))*(v2.lng()- v1.lng())){
                    crossingCounter += 1; // if the test point lies left to the edge, the ray crosses
                }
            }
        }
        return crossingCounter % 2 == 1; // check if the number of crossings is odd and return true
    }

    /**
     * Get the coordinate after a move in the direction of a certain angle.
     * @param startPosition the starting point
     * @param angle the direction of movement
     * @return a LngLat instance after the move
     */
    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        if (angle == 999){
            return startPosition;
        }
        else{
            double lngChange = SystemConstants.DRONE_MOVE_DISTANCE * Math.cos(Math.toRadians(angle));
            double latChange = SystemConstants.DRONE_MOVE_DISTANCE * Math.sin(Math.toRadians(angle));
            return new LngLat(startPosition.lng()+lngChange, startPosition.lat()+latChange);
        }
    }
}
