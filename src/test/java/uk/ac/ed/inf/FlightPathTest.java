package uk.ac.ed.inf;

import org.json.JSONObject;
import org.junit.Test;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FlightPathTest {

    FlightPath flightPath = new FlightPath();

    @Test
    public void deliveryTest() throws IOException {
        URL urlCentralArea = new URL("https://ilp-rest.azurewebsites.net/centralArea");
        URL urlNoFlyZones = new URL("https://ilp-rest.azurewebsites.net/noFlyZones");
        NamedRegion central = JsonController.fromJsonAll(urlCentralArea, NamedRegion.class);
        NamedRegion[] noFlyZones = JsonController.fromJsonAllRegions(urlNoFlyZones);
        LngLat startFromDomino = new LngLat(-3.1838572025299072,55.94449876875712);
        LngLat startFromSodeberg = new LngLat(-3.1940174102783203,55.94390696616939);
        LngLat startFromVegan = new LngLat (-3.202541470527649, 55.943284737579376);
        LngLat startFromSlice = new LngLat(-3.1912869215011597, 55.945535152517735);
        LngLat startFromHalal = new LngLat( -3.185428203143916, 55.945846113595);
        LngLat startFromWorldOfPizza = new LngLat(	-3.1798, 	55.9399);
        LngLat appletonTower = new LngLat(-3.186874, 55.944494);
        LngLat testPoint = new LngLat(-3.202534220527414,55.94328854718804);
        LngLat testPointKings = new LngLat( -3.1739,55.9227);
        LngLat testPointMeadows = new LngLat( -3.1913301,55.9394438);
        LngLat fromBypass = new LngLat( -3.203605,55.896510 );
        LngLat fromPentland = new LngLat(-3.224099,55.853128);
        LngLat fromGlasgow = new LngLat(-4.262607,55.859347);
        LngLat fromPeffer = new LngLat(-3.161159,55.928296);
        LngLat fromIKea = new LngLat(-3.171004,55.879750);
        LngLat fromRoyalObservatory = new LngLat(-3.1881, 55.9229);

        ArrayList<LngLat> pathHalalToApple = flightPath.searchAlgo(startFromHalal, appletonTower, noFlyZones, central, true);
        ArrayList<LngLat> pathAppleToWorld = flightPath.searchAlgo(pathHalalToApple.get(pathHalalToApple.size()-1), startFromWorldOfPizza, noFlyZones, central, false);
        ArrayList<LngLat> pathWorldToApple = flightPath.searchAlgo(pathAppleToWorld.get(pathAppleToWorld.size()-1), appletonTower, noFlyZones, central, true);
        ArrayList<LngLat> pathAppleToVegan = flightPath.searchAlgo(pathWorldToApple.get(pathWorldToApple.size()-1), startFromVegan, noFlyZones, central, false);
        ArrayList<LngLat> pathVeganToApple = flightPath.searchAlgo(pathAppleToVegan.get(pathAppleToVegan.size()-1), appletonTower, noFlyZones, central, true);
        JSONObject outputFeatureCollection = JsonController.generateLineStringJson();
        JsonController.addNodes(pathHalalToApple, outputFeatureCollection);
        pathAppleToWorld.remove(0);
        JsonController.addNodes(pathAppleToWorld, outputFeatureCollection);
        pathWorldToApple.remove(0);
        JsonController.addNodes(pathWorldToApple, outputFeatureCollection);
        pathAppleToVegan.remove(0);
        JsonController.addNodes(pathAppleToVegan, outputFeatureCollection);
        pathVeganToApple.remove(0);
        JsonController.addNodes(pathVeganToApple, outputFeatureCollection);
        JsonController.convertToGeoJSON(outputFeatureCollection, "testNewPaths2.geojson");


    }

    @Test
    public void deliverDay() throws IOException {
        URL urlCentralArea = new URL("https://ilp-rest.azurewebsites.net/centralArea");
        URL urlNoFlyZones = new URL("https://ilp-rest.azurewebsites.net/noFlyZones");
        NamedRegion central = JsonController.fromJsonAll(urlCentralArea, NamedRegion.class);
        NamedRegion[] noFlyZones = JsonController.fromJsonAllRegions(urlNoFlyZones);

    }


}