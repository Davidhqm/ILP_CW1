package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

public class CoordinateSystem implements LngLatHandling {
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        return Math.sqrt(Math.pow(startPosition.lng() - endPosition.lng(), 2) +
                Math.pow(startPosition.lat() - endPosition.lat(), 2));
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        int crossingCounter = 0;
        LngLat v1;
        LngLat v2;
        for (int i = 0; i < region.vertices().length; i++ ){
            v1 = region.vertices()[i];
            if (i == region.vertices().length - 1){
                v2 = region.vertices()[0];
            }
            else{
                v2 = region.vertices()[i+1];
            }

            if (isCloseTo(position, v1) || isCloseTo(position, v2)){
                return true;
            }
            if ( ((position.lat()<v1.lat()) != (position.lat()<v2.lat()))){
                if(position.lng() == v1.lng() + ((position.lat()-v1.lat())/(v2.lat()- v1.lat()))*(v2.lng()- v1.lng())){
                    return true;
                }
                if (position.lng() < v1.lng() + ((position.lat()-v1.lat())/(v2.lat()- v1.lat()))*(v2.lng()- v1.lng())){
                    crossingCounter += 1;
                }
            }
        }
        return crossingCounter % 2 == 1;
    }

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
