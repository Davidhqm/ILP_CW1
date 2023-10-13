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
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {
        orderToValidate.setOrderValidationCode(creditCardInfoChecker(orderToValidate.getCreditCardInformation(),
                orderToValidate.getOrderValidationCode()));

        orderToValidate.setOrderValidationCode(pizzaChecker(orderToValidate.getPizzasInOrder(), definedRestaurants,
                orderToValidate.getOrderDate(), orderToValidate.getOrderValidationCode(), orderToValidate.getPriceTotalInPence()));

        if (orderToValidate.getOrderValidationCode() == OrderValidationCode.UNDEFINED){
            orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
            orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        }else{
            orderToValidate.setOrderStatus(OrderStatus.INVALID);
        }
        return orderToValidate;
    }

    public OrderValidationCode creditCardInfoChecker(CreditCardInformation card, OrderValidationCode code){
        if ( !(card.getCreditCardNumber().matches("[0-9]+") && (card.getCreditCardNumber().length() == 16)) ){
            return OrderValidationCode.CARD_NUMBER_INVALID;
        }
        if ( !(card.getCvv().matches("[0-9]+") && (card.getCvv().length() == 3)) ){
            return OrderValidationCode.CVV_INVALID;
        }
        int expMonth = Integer.parseInt(card.getCreditCardExpiry().substring(0,2));
        int expYear = 2000+Integer.parseInt(card.getCreditCardExpiry().substring(3));
        LocalDate localDate = LocalDate.now();
        int monthNow = localDate.getMonthValue();
        int yearNow = localDate.getYear();

        if (expYear < yearNow){
            return OrderValidationCode.EXPIRY_DATE_INVALID;
        }else if (expYear == yearNow) {
            if (!(expMonth >= monthNow && expMonth <= 12)){
                return OrderValidationCode.EXPIRY_DATE_INVALID;
            }
        }else {
            if (!(expMonth >= 1 && expMonth <= 12)){
                return OrderValidationCode.EXPIRY_DATE_INVALID;
            }
        }
        return code;
    }

    public OrderValidationCode pizzaChecker(Pizza[] pizzas, Restaurant[] definedRestaurants, LocalDate orderDate, OrderValidationCode code, int price){
        if (pizzas.length > 4){
            return OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED; //If more than 4 pizzas ordered return this error
        }
        int actualTotalInPence = 0;
        ArrayList<Integer> restaurantNum = new ArrayList<>();

        for (int i = 0; i < pizzas.length; i++){
            searchingLoop:
            for (int j = 0; j < definedRestaurants.length; j++){   // loop through all the restaurants
                for (int k = 0; k < definedRestaurants[j].menu().length; k++){
                    if (pizzas[i].name().equals(definedRestaurants[j].menu()[k].name())){
                        restaurantNum.add(j);
                        actualTotalInPence += definedRestaurants[j].menu()[k].priceInPence();
                        break searchingLoop;
                    }
                }
            }
            if (restaurantNum.isEmpty() || ((i+1) != restaurantNum.size())){ // if one pizza is not found in any of the restaurant: undefined
                return OrderValidationCode.PIZZA_NOT_DEFINED;
            }
            if (restaurantNum.size()>1 && !restaurantNum.get(i-1).equals(restaurantNum.get(i))){
                return OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS;
            }
        }
        if (price != actualTotalInPence){
            return OrderValidationCode.TOTAL_INCORRECT;
        }
        if (!Arrays.stream(definedRestaurants[restaurantNum.get(0)].openingDays()).toList().contains(orderDate.getDayOfWeek())){
            return OrderValidationCode.RESTAURANT_CLOSED;
        }
        return code;
    }
}
