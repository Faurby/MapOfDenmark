package bfst21.pathfinding;

import bfst21.osm.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private int vertexAmount;
    private final HashMap<Node, Integer> coordsToIdMap = new HashMap<>();
    private final HashMap<Integer, Node> idToCoordsMap = new HashMap<>();
    private final HashMap<Integer, List<Edge>> adjacentEdges = new HashMap<>();

    public void createVertex(float x, float y, int id) {
        Node node = new Node(x, y, false);
        coordsToIdMap.put(node, id);
        idToCoordsMap.put(id, node);
        vertexAmount++;
    }

    public int getVertexID(Node node) {
        if (coordsToIdMap.containsKey(node)) {
            return coordsToIdMap.get(node);
        }
        return -1;
    }

    public Node getVertexNode(int id) {
        if (idToCoordsMap.containsKey(id)) {
            return idToCoordsMap.get(id);
        }
        return null;
    }

    public void addEdge(Node fromNode, Node toNode, int maxSpeed) {
        int fromID = getVertexID(fromNode);
        int toID = getVertexID(toNode);

        float distance = (float) fromNode.distTo(toNode);
        Edge edge1 = new Edge(fromID, toID, distance, maxSpeed);
        Edge edge2 = new Edge(toID, fromID, distance, maxSpeed);

        addEdge(toID, edge1);
        addEdge(toID, edge2);
        addEdge(fromID, edge1);
        addEdge(fromID, edge2);
    }

    private void addEdge(int vertexID, Edge edge) {
        List<Edge> edges = new ArrayList<>();
        if (adjacentEdges.containsKey(vertexID)) {
            edges = adjacentEdges.get(vertexID);
        }
        edges.add(edge);
        adjacentEdges.put(vertexID, edges);
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public List<Edge> getAdjacentEdges(int vertexID) {
        if (adjacentEdges.containsKey(vertexID)) {
            return adjacentEdges.get(vertexID);
        }
        return new ArrayList<>();
    }

    public HashMap<Integer, List<Edge>> getAdjacentEdges() {
        return adjacentEdges;
    }
}
