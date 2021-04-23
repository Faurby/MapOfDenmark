package bfst21.pathfinding;

import bfst21.osm.ElementIntIndex;

import java.io.Serializable;
import java.util.HashMap;


public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private int vertexAmount;
    private int edgeAmount;
    private final ElementIntIndex<Vertex> vertexIntIndex = new ElementIntIndex<>();
    private final HashMap<Vertex, Integer> vertexMap = new HashMap<>();

    public DirectedGraph() {
        this.edgeAmount = 0;
    }

    public void createVertex(float x, float y, int id) {
        Vertex vertex = new Vertex(x, y, id);
        vertexIntIndex.put(vertex);
        vertexMap.put(vertex, id);
        vertexAmount++;
    }

    public Vertex getVertex(float x, float y) {
        Vertex vertex = new Vertex(x, y, 1);

        if (vertexMap.containsKey(vertex)) {
            int id = vertexMap.get(vertex);
            return vertexIntIndex.get(id);
        }
        return null;
    }

    public Vertex getVertex(int id) {
        return vertexIntIndex.get(id);
    }

    public void addEdge(Vertex from, Vertex to, int maxSpeed) {
        float distance = (float) from.distTo(to);
        Edge edge = new Edge(from.getID(), to.getID(), distance, maxSpeed);
        from.addEdge(edge);
        to.addEdge(edge);

        edgeAmount++;
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public ElementIntIndex<Vertex> getVertexIntIndex() {
        return vertexIntIndex;
    }

    public HashMap<Vertex, Integer> getVertexMap() {
        return vertexMap;
    }
}
