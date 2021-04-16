package bfst21.pathfinding;

import edu.princeton.cs.algs4.Bag;


public class DirectedGraph {

    private final int vertexAmount;
    private int edgeAmount;
    private Bag<Edge> edges;
    private Bag<Vertex> vertices;

    public DirectedGraph(int vertexAmount) {
        this.vertexAmount = vertexAmount;
        this.edgeAmount = 0;
    }

    public Vertex getVertex(float x, float y) {
        for (Vertex vertex : vertices) {
            if (vertex.getX() == x && vertex.getY() == y) {
                return vertex;
            }
        }
        return null;
    }

    public void addEdge(Vertex from, Vertex to) {
        double maxSpeed = 55;
        Edge edge = new Edge(from, to, maxSpeed);
        from.addEdge(edge);
        to.addEdge(edge);
        edges.add(edge);

        edgeAmount++;
    }

    public int getEdgeAmount() {
        return edgeAmount;
    }

    public int getVertexAmount() {
        return vertexAmount;
    }
}
