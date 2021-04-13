package bfst21.osm;

import java.io.Serializable;


public class Node implements Serializable {

    private static final long serialVersionUID = -343957913094540189L;
    private final float x, y;

    public Node(float lat, float lon) {
        this.x = lon;
        this.y = -lat / 0.56f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRealY() {
        return -y * 0.56f;
    }

    //Distance between 2 nodes (lat, lon) by Haversine formula
    public double distTo(Node otherNode) {

        int R = 6371; //Radius of Earth

        float lat1 = this.getRealY();
        float lon1 = this.getX();
        float lat2 = otherNode.getRealY();
        float lon2 = otherNode.getX();

        double rLatDistance = Math.toRadians(lat2 - lat1);
        double rLonDistance = Math.toRadians(lon2 - lon1);

        double a = (Math.sin(rLatDistance / 2) * Math.sin(rLatDistance / 2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(rLonDistance / 2) * Math.sin(rLonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
