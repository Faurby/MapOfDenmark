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

}
