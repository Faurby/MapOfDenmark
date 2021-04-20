package bfst21.models;

import javafx.geometry.Point2D;


public class Util {

    public static double distance(Point2D point1, Point2D point2) {
        return distTo(point1.getY(), point1.getX(), point2.getY(), point2.getX());
    }

    //Distance between 2 nodes (lat, lon) by Haversine formula
    public static double distTo(double lat1, double lon1, double lat2, double lon2) {

        int R = 6371; //Radius of Earth
        double rLatDistance = Math.toRadians(lat2 - lat1);
        double rLonDistance = Math.toRadians(lon2 - lon1);

        double a = (Math.sin(rLatDistance / 2) * Math.sin(rLatDistance / 2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(rLonDistance / 2) * Math.sin(rLonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
