package bfst21.pathfinding;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;


public class Edge implements Drawable, Serializable {

    private static final long serialVersionUID = -8885206149678561745L;

    private final float distance, weight;
    private final int from, to, maxSpeed;

    public Edge(int from, int to, float distance, int maxSpeed) {
        this.distance = distance;
        this.from = from;
        this.to = to;
        this.maxSpeed = maxSpeed;
        this.weight = (distance * 60.0f / maxSpeed);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public float getWeight() {
        return weight;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        //gc.moveTo(from.getX(), from.getY());
        //gc.lineTo(to.getX(), to.getY());
    }
}
