package uk.ac.ed.inf;

public record OrderOutline(String orderNo,
                           String orderStatus,
                           String orderValidationCode,
                           int costInPence) {
}
