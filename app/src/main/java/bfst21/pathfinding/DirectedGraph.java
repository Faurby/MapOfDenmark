package bfst21.pathfinding;

import bfst21.models.Util;
import bfst21.osm.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * DirectedGraph is an edge-weighted directed graph.
 */
public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private final HashMap<Node, Integer> coordsToIdMap = new HashMap<>();

    private Vertex[] vertices = new Vertex[1];
    private Edge[] edges = new Edge[1];

    private int vertexAmount;
    private int edgeAmount;

    public void createVertex(float[] coords) {
        Node node = new Node(coords[0], coords[1]);

        if (!coordsToIdMap.containsKey(node)) {
            if (vertexAmount == vertices.length) {
                Vertex[] copy = new Vertex[vertices.length * 2];

                for (int i = 0; i < vertices.length; i++) {
                    copy[i] = vertices[i];
                }
                vertices = copy;
            }
            vertices[vertexAmount] = new Vertex(coords[0], coords[1]);
            coordsToIdMap.put(node, vertexAmount);

            vertexAmount++;
        }
    }

    public int getVertexID(float[] coords) {
        Node node = new Node(coords[0], coords[1]);

        if (coordsToIdMap.containsKey(node)) {
            return coordsToIdMap.get(node);
        }
        return -1;
    }

    public float[] getVertexCoords(int id) {
        Vertex vertex = vertices[id];

        if (vertex != null) {
            return vertex.getCoords();
        }
        return null;
    }

    public void addEdge(
            float[] fromCoords,
            float[] toCoords,
            int maxSpeed,
            boolean oneWay,
            boolean canDrive,
            boolean canBike,
            boolean canWalk) {

        int fromID = getVertexID(fromCoords);
        int toID = getVertexID(toCoords);
        float distance = (float) Util.distTo(fromCoords, toCoords);

        addEdge(fromID, toID, maxSpeed, distance, canDrive, canBike, canWalk);

        if (!oneWay) {
            addEdge(toID, fromID, maxSpeed, distance, canDrive, canBike, canWalk);
        }
    }

    private void addEdge(
            int fromID,
            int toID,
            int maxSpeed,
            float distance,
            boolean canDrive,
            boolean canBike,
            boolean canWalk) {

        Edge edge = new Edge(fromID, toID, distance, maxSpeed, canDrive, canBike, canWalk);

        if (edgeAmount == edges.length) {
            Edge[] copy = new Edge[edges.length * 2];

            for (int i = 0; i < edges.length; i++) {
                copy[i] = edges[i];
            }
            edges = copy;
        }
        edges[edgeAmount] = edge;

        Vertex vertexFrom = vertices[fromID];
        Vertex vertexTo = vertices[toID];

        if (vertexFrom != null) {
            vertexFrom.addEdge(edgeAmount);
        }
        if (vertexTo != null) {
            vertexTo.addEdge(edgeAmount);
        }
        edgeAmount++;
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public List<Edge> getAdjacentEdges(int vertexID) {
        List<Edge> edgeList = new ArrayList<>();

        Vertex vertex = vertices[vertexID];
        if (vertex != null) {
            for (int id : vertex.getEdges()) {
                Edge edge = edges[id];
                edgeList.add(edge);
            }
        }
        return edgeList;
    }

    public Edge getEdge(int id) {
        return edges[id];
    }

    public Vertex[] getVertices() {
        return vertices;
    }
}
