package uk.ac.ed.inf;

public record DroneMovement(String orderNo,
                            double fromLongitude,
                            double fromLatitude,
                            double angle,
                            double toLongitude,
                            double toLatitude) {


}
