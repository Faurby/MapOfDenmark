package bfst21.pathfinding;

import java.io.Serializable;


public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private final Bag<Vertex> vertices;
    private final int vertexAmount;
    private int edgeAmount;

    public DirectedGraph(int vertexAmount) {
        this.vertexAmount = vertexAmount;
        this.edgeAmount = 0;
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

    public void addEdge(Vertex from, Vertex to, int maxSpeed) {
        float distance = (float) from.distTo(to);
        Edge edge = new Edge(from.getID(), to.getID(), distance, maxSpeed);
        from.addEdge(edge);
        to.addEdge(edge);
        //vertices.add(from);
        //vertices.add(to);

        edgeAmount++;
    }

    public int getEdgeAmount() {
        return edgeAmount;
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public Bag<Vertex> getVertices() {
        return vertices;
    }
}
