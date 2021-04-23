package bfst21.pathfinding;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;


public class Edge implements Serializable {

    private static final long serialVersionUID = -8885206149678561745L;

    private final float weight;
    private final int from, to;

    public Edge(int from, int to, float distance, int maxSpeed) {
        this.from = from;
        this.to = to;
        this.weight = (distance * 60.0f / maxSpeed);
    }

    public void draw(DirectedGraph directedGraph, GraphicsContext gc) {
        Coordinate fromCoords = directedGraph.getVertexCoords(from);
        Coordinate toCoords = directedGraph.getVertexCoords(to);

        if (fromCoords != null && toCoords != null) {
            gc.moveTo(fromCoords.getX(), fromCoords.getY());
            gc.lineTo(toCoords.getX(), toCoords.getY());
        }
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public float getWeight() {
        return weight;
    }
}
