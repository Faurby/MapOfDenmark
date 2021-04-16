package bfst21.pathfinding;

public class Edge {

    private Vertex from;
    private Vertex to;
    private double weight;
    private double maxSpeed;
    private double distance;

    public Edge(Vertex from, Vertex to, double maxSpeed) {
        this.distance = from.distTo(to);
        this.from = from;
        this.to = to;
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getDistance() {
        return distance;
    }
}
