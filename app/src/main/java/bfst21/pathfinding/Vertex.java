package bfst21.pathfinding;

import java.io.Serializable;


/**
 * Vertex used in DirectedGraph.
 */
public class Vertex implements Serializable {

    private static final long serialVersionUID = 5389761446007729723L;
    private final float x, y;

    private int[] edges = new int[0];

    public Vertex(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void addEdge(int id) {
        int length = edges.length;

        int[] copy = new int[length + 1];
        for (int i = 0; i < edges.length; i++) {
            copy[i] = edges[i];
        }
        edges = copy;
        edges[length] = id;
    }

    public float[] getCoords() {
        return new float[]{x, y};
    }

    public int[] getEdges() {
        return edges;
    }
}