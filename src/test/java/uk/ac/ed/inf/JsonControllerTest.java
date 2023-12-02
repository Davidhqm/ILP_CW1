package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;


public class JsonControllerTest {


    @Test
    public void parse() {
    }


    @Test
    public void fromJsonAll() throws IOException {
        URL url = new URL("https://ilp-rest.azurewebsites.net/centralArea");
        NamedRegion central = JsonController.fromJsonAll(url, NamedRegion.class);
        System.out.println(central.vertices()[0]);
    }

    @Test
    public void fromJsonAllOrders() throws IOException {
        URL url = new URL("https://ilp-rest.azurewebsites.net/orders/2023-09-10");
        Order[] orders = JsonController.fromJsonAllOrders(url);
        System.out.println(orders.length);
    }

    @Test
    public void fromJsonAllRegions() throws IOException {
        URL url = new URL("https://ilp-rest.azurewebsites.net/noFlyZones");
        NamedRegion[] noFlyZones = JsonController.fromJsonAllRegions(url);
        System.out.println(noFlyZones[0].name());
    }

    @Test
    public void testFeatureCollection(){
        JsonController.convertToGeoJSON(JsonController.generateLineStringJson(), "test.geojson");
    }

}