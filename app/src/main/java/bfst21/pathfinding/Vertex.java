package bfst21.pathfinding;


public class Vertex {

    private final float x, y;

    private int[] edges = new int[0];

    public Vertex(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float[] getCoords() {
        return new float[]{x, y};
    }

    public int[] getEdges() {
        return edges;
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
}