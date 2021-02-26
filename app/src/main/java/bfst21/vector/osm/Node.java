package bfst21.vector.osm;

import java.io.Serializable;

public class Node implements Serializable {
    private static final long serialVersionUID = -343957913094540189L;
    private float x, y;
    private transient long id;

    public Node(long id, float lat, float lon) {
        this.id = id;
        this.x = lon;
        this.y = -lat/0.56f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

	public long getID() {
		return id;
	}
}
