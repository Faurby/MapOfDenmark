package bfst21.pathfinding;

import bfst21.osm.Node;
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
        Node fromNode = directedGraph.getVertexNode(from);
        Node toNode = directedGraph.getVertexNode(to);

        if (fromNode != null && toNode != null) {
            gc.moveTo(fromNode.getX(), fromNode.getY());
            gc.lineTo(toNode.getX(), toNode.getY());
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
