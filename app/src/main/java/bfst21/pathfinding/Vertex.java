package bfst21.pathfinding;

import java.io.Serializable;


public class Vertex implements Serializable {

    private static final long serialVersionUID = 3949086273175715882L;

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

    //Distance between 2 nodes (lat, lon) by Haversine formula
    public double distTo(Vertex otherVertex) {

        int R = 6371; //Radius of Earth

        float lat1 = this.getRealY();
        float lon1 = this.getX();
        float lat2 = otherVertex.getRealY();
        float lon2 = otherVertex.getX();

        double rLatDistance = Math.toRadians(lat2 - lat1);
        double rLonDistance = Math.toRadians(lon2 - lon1);

        double a = (Math.sin(rLatDistance / 2) * Math.sin(rLatDistance / 2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(rLonDistance / 2) * Math.sin(rLonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
