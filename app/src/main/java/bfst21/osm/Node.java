package bfst21.osm;

import bfst21.models.Util;

import java.io.Serializable;
import java.util.Objects;


public class Node implements Serializable {

    private static final long serialVersionUID = -343957913094540189L;
    private final float x, y;

    public Node(float lon, float lat, boolean convertLat) {
        this.x = lon;

        if (convertLat) {
            this.y = -lat / 0.56f;
        } else {
            this.y = lat;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node that = (Node) o;
        return Float.compare(that.getX(), x) == 0 && Float.compare(that.getY(), y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
