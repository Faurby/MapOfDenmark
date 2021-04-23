package bfst21.pathfinding;

import bfst21.models.Util;

import java.io.Serializable;
import java.util.Objects;


public class Vertex implements Serializable {

    private static final long serialVersionUID = 7453492287212788153L;

    private final float x, y;
    private final int id;
    private final Bag<Edge> edges;

    public Vertex(float x, float y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.edges = new Bag<>();
    }

    public int getID() {
        return id;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public Bag<Edge> getEdges() {
        return edges;
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

    public double distTo(Vertex otherVertex) {
        float lat1 = this.getRealY();
        float lon1 = this.getX();
        float lat2 = otherVertex.getRealY();
        float lon2 = otherVertex.getX();

        return Util.distTo(lat1, lon1, lat2, lon2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex that = (Vertex) o;
        return Float.compare(that.x, x) == 0 && Float.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
