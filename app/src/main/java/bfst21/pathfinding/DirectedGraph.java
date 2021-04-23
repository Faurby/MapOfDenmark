package bfst21.pathfinding;

import bfst21.models.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private int vertexAmount;
    private final HashMap<Coordinate, Integer> coordsToIdMap = new HashMap<>();
    private final HashMap<Integer, Coordinate> idToCoordsMap = new HashMap<>();
    private final HashMap<Integer, List<Edge>> adjacentEdges = new HashMap<>();

    public void createVertex(float x, float y, int id) {
        Coordinate coordinate = new Coordinate(x, y);
        coordsToIdMap.put(coordinate, id);
        idToCoordsMap.put(id, coordinate);
        vertexAmount++;
    }

    public int getVertexID(Coordinate coordinate) {
        if (coordsToIdMap.containsKey(coordinate)) {
            return coordsToIdMap.get(coordinate);
        }
        return -1;
    }

    public int getVertexID(float x, float y) {
        Coordinate coordinate = new Coordinate(x, y);
        return getVertexID(coordinate);
    }

    public Coordinate getVertexCoords(int id) {
        if (idToCoordsMap.containsKey(id)) {
            return idToCoordsMap.get(id);
        }
        return null;
    }

    public void addEdge(Coordinate fromCoords, Coordinate toCoords, int maxSpeed) {
        int fromID = getVertexID(fromCoords);
        int toID = getVertexID(toCoords);

        float distance = (float) Util.distTo(fromCoords.getX(), fromCoords.getY(), toCoords.getX(), toCoords.getY());
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
