package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

public class OrderValidator implements OrderValidation {

    public OrderValidator(){}

    /**
     * Update the OrderValidationCode and OrderStatus of an order.
     * @param orderToValidate the order to be validated
     * @param definedRestaurants all the restaurants retrieved from server
     * @return the updated order
     */
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        orderToValidate.setOrderValidationCode(creditCardInfoChecker(orderToValidate.getCreditCardInformation(),
                orderToValidate.getOrderValidationCode())); //validate the credit card information

        orderToValidate.setOrderValidationCode(pizzaChecker(orderToValidate.getPizzasInOrder(), definedRestaurants,
                orderToValidate.getOrderDate(), orderToValidate.getOrderValidationCode(), orderToValidate.getPriceTotalInPence()));
                //validate the pizzas and restaurants information
        if (orderToValidate.getOrderValidationCode() == OrderValidationCode.UNDEFINED){
            orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
            orderToValidate.setOrderStatus(OrderStatus.DELIVERED);
        }else{
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
        }
        return orderToValidate;
    }

    /**
     * Validate all the orders in the input array and return a list of valid orders of the day.
     * Populate the ArrayList orderOutlines
     * @param orders all orders from a day
     * @param restaurants an array of restaurants retrieved from server
     * @param orderOutlines an ArrayList of orderOutline records
     * @return an ArrayList of all valid orders.
     */
    public ArrayList<Order> filterAllValidOn(Order[] orders, Restaurant[] restaurants, ArrayList<OrderOutline> orderOutlines){
        ArrayList<Order> validOrders = new ArrayList<>();
        for (Order order : orders) {
            Order validatedOrder = validateOrder(order, restaurants);//update the status of orders
            OrderOutline currentOrderOutline = new OrderOutline(validatedOrder.getOrderNo(), validatedOrder.getOrderStatus().name(),
                    validatedOrder.getOrderValidationCode().name(), validatedOrder.getPriceTotalInPence());
            orderOutlines.add(currentOrderOutline);
            if (validatedOrder.getOrderValidationCode() == OrderValidationCode.NO_ERROR){
                    validOrders.add(validatedOrder);
            }
        }
        return validOrders;
    }

    /**
     * Get the restaurant of this order
     * @param order a valid order to evaluate
     * @param restaurants all restaurants retrieved from the server
     * @return the restaurant of this order
     */
    public Restaurant getOrderRestaurant(Order order, Restaurant[] restaurants){
        for(Restaurant restaurant : restaurants){
            if(Arrays.asList(restaurant.menu()).contains(order.getPizzasInOrder()[0])){
                return restaurant;
            }
        }
        return null;
    }

    /**
     * Check if the credit card information given is correct.
     * @param card the credit card information
     * @param code the original OrderValidationCode
     * @return the corresponding error code related to the card
     */
    public OrderValidationCode creditCardInfoChecker(CreditCardInformation card, OrderValidationCode code){
        if ( !(card.getCreditCardNumber().matches("[0-9]+") && (card.getCreditCardNumber().length() == 16)) ){
            return OrderValidationCode.CARD_NUMBER_INVALID; //check if the card number consists of 16 numbers exclusively
        }
        if ( !(card.getCvv().matches("[0-9]+") && (card.getCvv().length() == 3)) ){
            return OrderValidationCode.CVV_INVALID; // check if the cvv code consists of 3 numbers exclusively
        }
        int expMonth = Integer.parseInt(card.getCreditCardExpiry().substring(0,2));
        int expYear = 2000+Integer.parseInt(card.getCreditCardExpiry().substring(3));
        LocalDate localDate = LocalDate.now();
        int monthNow = localDate.getMonthValue();
        int yearNow = localDate.getYear();

        if (expYear < yearNow){
            return OrderValidationCode.EXPIRY_DATE_INVALID; //year smaller
        }else if (expYear == yearNow) {
            if (!(expMonth >= monthNow && expMonth <= 12)){// same year but month is either smaller than now or larger than 12(an illegal month)
                return OrderValidationCode.EXPIRY_DATE_INVALID;
            }
        }else {
            if (!(expMonth >= 1 && expMonth <= 12)){// year larger but month is illegal(not bounded between 1 and 12)
                return OrderValidationCode.EXPIRY_DATE_INVALID;
            }
        }
        return code;  
    }

    /**
     * Check if the pizzas and restaurants information of an order have errors.
     * @param pizzas the array of pizza from the order
     * @param definedRestaurants all the restaurants retrieved from server
     * @param orderDate the date the order is made
     * @param code the original OrderValidationCode
     * @param price the total price in pence listed in the order
     * @return the corresponding error code related to pizzas or restaurants
     */
    public OrderValidationCode pizzaChecker(Pizza[] pizzas, Restaurant[] definedRestaurants, LocalDate orderDate, OrderValidationCode code, int price){
        if (pizzas.length > 4){
            return OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED; //If more than 4 pizzas ordered return this error
        }
        int actualTotalInPence = 0;
        ArrayList<Integer> restaurantNum = new ArrayList<>();

        for (int i = 0; i < pizzas.length; i++){ // loop through the pizzas ordered
            searchingLoop:
            for (int j = 0; j < definedRestaurants.length; j++){   // loop through all the restaurants
                for (int k = 0; k < definedRestaurants[j].menu().length; k++){ // loop through all the pizzas on the menu of restaurant[j]
                    if (pizzas[i].name().equals(definedRestaurants[j].menu()[k].name())){ // pizza[i] is found
                        restaurantNum.add(j);
                        actualTotalInPence += definedRestaurants[j].menu()[k].priceInPence();// add the actual price defined in menu
                        break searchingLoop; // break out to search for the next pizza ordered
                    }
                }
            }
            if (restaurantNum.isEmpty() || ((i+1) != restaurantNum.size())){ // whenever there is a pizza is not found in any of the restaurant: pizza_undefined
                return OrderValidationCode.PIZZA_NOT_DEFINED;
            }
            if (restaurantNum.size()>1 && !restaurantNum.get(i-1).equals(restaurantNum.get(i))){ // if two different restaurants appeared
                return OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS;
            }
        }
        if (price != actualTotalInPence + 100){ // actual total price does not equal to the listed price in order: total_incorrect
            return OrderValidationCode.TOTAL_INCORRECT;
        }
        if (!Arrays.stream(definedRestaurants[restaurantNum.get(0)].openingDays()).toList().contains(orderDate.getDayOfWeek())){
            return OrderValidationCode.RESTAURANT_CLOSED;
        }
        return code;
    }
}
