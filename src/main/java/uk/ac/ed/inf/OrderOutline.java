package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;

public class OrderOutline {

    private final String orderNo;
    private final String orderStatus;
    private final String orderValidationCode;
    private final int costInPence;

    public OrderOutline(String orderNo, String orderStatus, String orderValidationCode, int costInPence){
        this.orderNo = orderNo;
        this.orderStatus = orderStatus;
        this.orderValidationCode = orderValidationCode;
        this.costInPence = costInPence;
    }

}
