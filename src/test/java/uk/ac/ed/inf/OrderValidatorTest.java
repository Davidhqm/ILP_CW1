package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class OrderValidatorTest {

    OrderValidator host = new OrderValidator();

    @Test
    public void localDateConverter(){
        LocalDate date = LocalDate.parse("2022-11-05");
        LocalDate date2 = LocalDate.parse("2022-11-06");
        System.out.println(date == date2);
    }

    @Test
    public void filterOutOnADay() throws IOException {
        URL url = new URL("https://ilp-rest.azurewebsites.net/orders/2023-09-02");
        URL url1 = new URL("https://ilp-rest.azurewebsites.net/restaurants");
        Order[] orders = JsonController.fromJsonAllOrders(url);
        Restaurant[] restaurants = JsonController.fromJsonAllRestaurants(url1);
        ArrayList<Order> test = host.filterAllValidOn(orders, restaurants);
        for(Order order : orders){
            System.out.println(order.getOrderValidationCode());
        }

    }

}