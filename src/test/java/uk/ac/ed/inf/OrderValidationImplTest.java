package uk.ac.ed.inf;

import java.time.DayOfWeek;
import java.time.LocalDate;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.*;

public class OrderValidationImplTest extends TestCase {
    private final OrderValidator impl = new OrderValidator();
    private final Restaurant [] restaurants = {
            new Restaurant("Civerinos Slice",
                    new LngLat(-3.1912869215011597, 55.945535152517735),
                    new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY},
                    new Pizza[] {new Pizza(	"Margarita", 1000), new Pizza("Calzone", 1400)}),

            new Restaurant("Sora Lella Vegan Restaurant",
                    new LngLat(	-3.202541470527649, 55.943284737579376),
                    new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY},
                    new Pizza[] {new Pizza(	"Meat Lover", 1400), new Pizza("Vegan Delight", 1100)}),

    };

    private Order newValidOrder() {
        return new Order("101010",
                LocalDate.of(2023, 10,2),
                2500,
                new Pizza[] {new Pizza(	"Margarita", 1000), new Pizza("Calzone", 1400)},
                new CreditCardInformation("4005519200000004", "10/25", "123"));
    }

    public void testValidateOrder() {
        Order order = newValidOrder();

        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.VALID_BUT_NOT_DELIVERED, order.getOrderStatus());
        assertEquals(OrderValidationCode.NO_ERROR, order.getOrderValidationCode());
    }

    public void testValidateOrderInvalidCardNumber() {
        Order order = newValidOrder();

        order.getCreditCardInformation().setCreditCardNumber("400551920x000001");
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());

        // Incorrect digits
        order.getCreditCardInformation().setCreditCardNumber("400551920000");
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());

        // Luhn algorithm
        order.getCreditCardInformation().setCreditCardNumber("4005519200000001");
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());
    }

    public void testValidateOrderInvalidExpiry() {
        Order order = newValidOrder();

        // BS data
        order.getCreditCardInformation().setCreditCardExpiry("10/24");
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.VALID_BUT_NOT_DELIVERED, order.getOrderStatus());
        assertEquals(OrderValidationCode.NO_ERROR, order.getOrderValidationCode());

        // Expired
        order.getCreditCardInformation().setCreditCardExpiry("10/20");
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, order.getOrderValidationCode());
    }

    public void testValidateOrderInvalidCvv() {
        Order order = newValidOrder();

        // BS data
        order.getCreditCardInformation().setCvv("xxx");
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.CVV_INVALID, order.getOrderValidationCode());

        // Incorrect digits
        order.getCreditCardInformation().setCvv("12");
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.CVV_INVALID, order.getOrderValidationCode());
    }

    public void testValidateOrderTotalIncorrect() {
        Order order = newValidOrder();

        order.setPriceTotalInPence(10000);
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.TOTAL_INCORRECT, order.getOrderValidationCode());
    }

    public void testValidateOrderPizzaNotDefined() {
        Order order = newValidOrder();

        order.getPizzasInOrder()[1] = new Pizza("OOXX", 1400);
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.PIZZA_NOT_DEFINED, order.getOrderValidationCode());
    }

    public void testValidateOrderPizzaFromMultipleRestaurants() {
        Order order = newValidOrder();

        // Total incorrect takes priority
        order.getPizzasInOrder()[1] = new Pizza("Meat Lover", 1400);
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS, order.getOrderValidationCode());
    }

    public void testValidateOrderMaxPizzaCountExceed() {
        final int numPizzas = 5;
        Order order = newValidOrder();
        Pizza[] pizzas = new Pizza[numPizzas];
        int total = SystemConstants.ORDER_CHARGE_IN_PENCE;

        for (int i = 0; i < numPizzas; i++) {
            pizzas[i] = new Pizza("Margarita", 1000);
            total += 1000;
        }
        order.setPizzasInOrder(pizzas);
        order.setPriceTotalInPence(total);
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED, order.getOrderValidationCode());
    }
    public void testValidateOrderRestaurantClosed() {
        Order order = newValidOrder();

        order.setOrderDate(LocalDate.of(2023, 10, 5));
        order = impl.validateOrder(order, restaurants);
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        assertEquals(OrderValidationCode.RESTAURANT_CLOSED, order.getOrderValidationCode());
    }
}