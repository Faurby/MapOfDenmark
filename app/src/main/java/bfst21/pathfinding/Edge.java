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
        float[] fromCoords = directedGraph.getVertexNode(from);
        float[] toCoords = directedGraph.getVertexNode(to);

        if (fromCoords != null && toCoords != null) {
            gc.moveTo(fromCoords[0], fromCoords[1]);
            gc.lineTo(toCoords[0], toCoords[1]);
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
