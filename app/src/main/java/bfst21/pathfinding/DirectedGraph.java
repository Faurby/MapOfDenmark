package bfst21.pathfinding;

import java.io.Serializable;


public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

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

    public Vertex getVertex(float x, float y, int id) {
        for (Vertex vertex : vertices) {
            if (vertex.getX() == x && vertex.getY() == y) {
                return vertex;
            }
        }
        return new Vertex(x, y, id);
    }

    public Vertex getVertex(int id) {
        for (Vertex vertex : vertices) {
            if (vertex.getID() == id) {
                return vertex;
            }
        }
        return null;
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
