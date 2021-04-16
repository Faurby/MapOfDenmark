package bfst21.pathfinding;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;


public class Edge implements Drawable {

    private final double distance;
    private final Vertex from;
    private final Vertex to;
    private double weight;
    private double maxSpeed;

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

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(from.getX(), from.getY());
        gc.lineTo(to.getX(), to.getY());
    }
}
