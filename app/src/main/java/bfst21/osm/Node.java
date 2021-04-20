package bfst21.osm;

import bfst21.models.Util;
import java.io.Serializable;


public class Node implements Serializable {

    private static final long serialVersionUID = -343957913094540189L;
    private final float x, y;

    public Node(float lon, float lat) {
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

    public double distTo(Node other) {
        float lat1 = this.getRealY();
        float lon1 = this.getX();
        double lat2 = other.getRealY();
        double lon2 = other.getX();

        return Util.distTo(lat1, lon1, lat2, lon2);
    }
}
