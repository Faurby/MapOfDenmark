package bfst21.pathfinding;

import bfst21.osm.ElementIntIndex;

import java.io.Serializable;
import java.util.HashMap;


public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private int vertexAmount;
    private int edgeAmount;
    private ElementIntIndex<Vertex> vertexIntIndex = new ElementIntIndex<>();
    private HashMap<VertexPoint, Integer> vertexMap = new HashMap<>();
    private Dijkstra dijkstra;

    public DirectedGraph() {
        this.edgeAmount = 0;
    }

    public void createVertex(float x, float y, int id) {
        vertexIntIndex.put(new Vertex(x, y, id));
        vertexMap.put(new VertexPoint(x, y), id);
        vertexAmount++;
    }

    public Vertex getVertex(float x, float y) {
        VertexPoint vertexPoint = new VertexPoint(x, y);

        if (vertexMap.containsKey(vertexPoint)) {
            int id = vertexMap.get(vertexPoint);
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

    public int getEdgeAmount() {
        return edgeAmount;
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public ElementIntIndex<Vertex> getVertexIntIndex() {
        return vertexIntIndex;
    }
}
