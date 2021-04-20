package bfst21.osm;

import bfst21.models.Util;
import bfst21.pathfinding.Vertex;
import javafx.geometry.Point2D;

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

    public double distance(Node other) {
        double p = 0.017453292519943295;            // Math.PI / 180
        double a = 0.5 - Math.cos((other.getRealY() - this.getRealY()) * p)/2 +
                Math.cos(this.getRealY() * p) * Math.cos(other.getRealY()) * p *
                (1 - Math.cos((other.getX() - this.getX()) * p))/2;
        return 12742 * Math.asin(Math.sqrt(a));     // 2 * R; R = 6371 km
    }

    public double distTo(Node other) {

        float lat1 = this.getRealY();
        float lon1 = this.getX();
        double lat2 = other.getRealY();
        double lon2 = other.getX();

        return Util.distTo(lat1, lon1, lat2, lon2);
    }
}
