package bfst21.vector.osm;

import java.io.Serializable;


public class Node extends Element implements Serializable {

    private static final long serialVersionUID = -343957913094540189L;
    private float x, y;

    public Node(long id, float lat, float lon) {
        super(id);
        this.x = lon;
        this.y = -lat / 0.56f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public ElementType getType() {
        return ElementType.NODE;
    }
}
