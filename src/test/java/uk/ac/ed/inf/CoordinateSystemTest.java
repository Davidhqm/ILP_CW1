package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.data.LngLat;

import static org.junit.Assert.*;

public class CoordinateSystemTest {

    @Test
    public void testGetAngle(){
        CoordinateSystem handler = new CoordinateSystem();
        LngLat startPoint = new LngLat(	-3.1869, 	55.9445);
        LngLat nextPoint = handler.nextPosition(startPoint, 337.5);
        double angle = handler.getAngle(startPoint, nextPoint);
        System.out.println(angle);
    }
}