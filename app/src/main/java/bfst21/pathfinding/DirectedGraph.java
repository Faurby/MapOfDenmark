package bfst21.pathfinding;

import edu.princeton.cs.algs4.Bag;


public class DirectedGraph {

    private final Bag<Edge> edges;
    private final Bag<Vertex> vertices;
    private final int vertexAmount;
    private int edgeAmount;

    public DirectedGraph(int vertexAmount) {
        this.vertexAmount = vertexAmount;
        this.edgeAmount = 0;
        this.edges = new Bag<>();
        this.vertices = new Bag<>();
    }

    public Vertex getVertex(float x, float y) {
        for (Vertex vertex : vertices) {
            if (vertex.getX() == x && vertex.getY() == y) {
                return vertex;
            }
        }
        return new Vertex(x, y);
    }

    public void addEdge(Vertex from, Vertex to, double maxSpeed) {
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

    public Bag<Edge> getEdges() {
        return edges;
    }

    public Bag<Vertex> getVertices() {
        return vertices;
    }
}
