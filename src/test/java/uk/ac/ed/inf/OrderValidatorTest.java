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
        ArrayList<OrderOutline> orderOutlines = new ArrayList<>();
        ArrayList<Order> test = host.filterAllValidOn(orders, restaurants, orderOutlines);
        System.out.println(test.size());
        for(OrderOutline orderOutline : orderOutlines){
            System.out.println(orderOutline.orderNo());
        }
    }

    @Test
    public void getRestaurantTest() throws IOException {
        URL urlOrders = new URL("https://ilp-rest.azurewebsites.net/orders/2023-09-02");
        URL urlRestaurants = new URL("https://ilp-rest.azurewebsites.net/restaurants");
        Order[] orders = JsonController.fromJsonAllOrders(urlOrders);
        Restaurant[] restaurants = JsonController.fromJsonAllRestaurants(urlRestaurants);
        ArrayList<OrderOutline> orderOutlines = new ArrayList<>();
        ArrayList<Order> allValidOrders = host.filterAllValidOn(orders, restaurants, orderOutlines);
        System.out.println(allValidOrders.size());


        Restaurant targetRestaurant = host.getOrderRestaurant(allValidOrders.get(4), restaurants);
        System.out.println(allValidOrders.get(4).getOrderNo());
        System.out.println(targetRestaurant.name());

    }

}