package bfst21.pathfinding;

import java.io.Serializable;


/**
 * Vertex used in DirectedGraph.
 */
public class Vertex implements Serializable {

    private static final long serialVersionUID = 5389761446007729723L;
    private final float x, y;

    private int[] adjacentEdges = new int[0];

    public Vertex(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Add Edge id to the array of adjacent edges.
     * <p>
     * Array size is increased by 1 before inserting the id.
     * When building the graph we don't know how many edges each vertex has.
     * The array is gradually resized to avoid empty slots in the array.
     */
    public void addEdge(int id) {
        int length = adjacentEdges.length;

        int[] copy = new int[length + 1];
        for (int i = 0; i < adjacentEdges.length; i++) {
            copy[i] = adjacentEdges[i];
        }
        adjacentEdges = copy;
        adjacentEdges[length] = id;
    }

    public float[] getCoords() {
        return new float[]{x, y};
    }

    public int[] getAdjacentEdges() {
        return adjacentEdges;
    }
}