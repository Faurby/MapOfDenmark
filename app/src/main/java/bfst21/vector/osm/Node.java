package bfst21.vector.osm;

public class Node {
    private float x,y;

    public Node(float lat, float lon) {
        this.x = lon;
        this.y = -lat/0.56f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
