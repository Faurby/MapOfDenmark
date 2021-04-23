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

    private Bag<Edge>[] adj;

    public void createVertex(float x, float y, int id) {
        Coordinate coordinate = new Coordinate(x, y);
        coordsToIdMap.put(coordinate, id);
        idToCoordsMap.put(id, coordinate);
        vertexAmount++;
    }

    private void createEdgeBag() {
        if (adj == null && vertexAmount > 0) {
            adj = (Bag<Edge>[]) new Bag[vertexAmount];
            System.out.println("Create with size: "+vertexAmount);
        }
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
        createEdgeBag();

        int fromID = getVertexID(fromCoords);
        int toID = getVertexID(toCoords);

        float distance = (float) Util.distTo(fromCoords.getX(), fromCoords.getY(), toCoords.getX(), toCoords.getY());
        Edge edge1 = new Edge(fromID, toID, distance, maxSpeed);
        Edge edge2 = new Edge(toID, fromID, distance, maxSpeed);

        System.out.println("Ids: "+toID+" "+fromID);

        adj[toID].add(edge1);
        adj[toID].add(edge2);
        adj[fromID].add(edge1);
        adj[fromID].add(edge2);
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public Bag<Edge>[] getAdj() {
        return adj;
    }
}
