package bfst21.pathfinding;

import bfst21.models.DistanceUtil;
import bfst21.osm.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * DirectedGraph is an edge-weighted directed graph.
 * <p>
 * This graph is gradually built by creating vertices and edges between them.
 * We add a Vertex or Edge to its array and resize it if the size limit is reached.
 * When the graph has been fully built, we clean up the arrays to avoid empty slots.
 * <p>
 * coordsToIdMap is used to determine if a vertex exists with the coordinates of a Node.
 */
public class DirectedGraph implements Serializable {

    private static final long serialVersionUID = -2665514385590129687L;

    private final HashMap<Node, Integer> coordsToIdMap = new HashMap<>();

    private Vertex[] vertices = new Vertex[1];
    private Edge[] edges = new Edge[1];

    private int vertexAmount, edgeAmount;

    /**
     * Clean up vertices and edges by removing unused slots in the array.
     * We count how many elements are actually present, then create a
     * new array with the correct size and copy all elements over.
     * This decreases overall memory usage.
     */
    public void cleanUp() {

        int actualVertexAmount = 0;
        int actualEdgeAmount = 0;

        for (int i = 0; i < vertices.length; i++) {
            Vertex vertex = vertices[i];

            if (vertex != null) {
                actualVertexAmount++;
            }
        }
        for (int i = 0; i < edges.length; i++) {
            Edge edge = edges[i];

            if (edge != null) {
                actualEdgeAmount++;
            }
        }
        Vertex[] verticesCopy = new Vertex[actualVertexAmount];
        Edge[] edgesCopy = new Edge[actualEdgeAmount];

        int vertexCount = 0;
        for (int i = 0; i < vertices.length; i++) {
            Vertex vertex = vertices[i];
            if (vertex != null) {
                verticesCopy[vertexCount] = vertex;
                vertexCount++;
            }
        }
        int edgeCount = 0;
        for (int i = 0; i < edges.length; i++) {
            Edge edge = edges[i];
            if (edge != null) {
                edgesCopy[edgeCount] = edge;
                edgeCount++;
            }
        }
        vertices = verticesCopy;
        edges = edgesCopy;

        vertexAmount = actualVertexAmount;
        edgeAmount = actualEdgeAmount;
    }

    /**
     * Create new vertex with the given coordinates.
     * It is only created if no vertex exists at the given coordinates.
     * Resize the array of vertices if necessary.
     */
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

    /**
     * @return id of a vertex with the given coordinates.
     */
    public int getVertexID(float[] coords) {
        Node node = new Node(coords[0], coords[1]);

        if (coordsToIdMap.containsKey(node)) {
            return coordsToIdMap.get(node);
        }
        return -1;
    }

    /**
     * @return coordinates of a vertex with the given id.
     */
    public float[] getVertexCoords(int id) {
        Vertex vertex = vertices[id];

        if (vertex != null) {
            return vertex.getCoords();
        }
        return null;
    }

    /**
     * Add an Edge to the graph.
     * <p>
     * Before creating the edge, we need to determine if it is
     * necessary to create another edge in the opposite direction.
     * This is determined using the oneWay and oneWayBike values.
     */
    public void addEdge(String name,
                        float[] fromCoords,
                        float[] toCoords,
                        int maxSpeed,
                        boolean junction,
                        boolean oneWay,
                        boolean oneWayBike,
                        boolean canDrive,
                        boolean canBike,
                        boolean canWalk) {

        int fromID = getVertexID(fromCoords);
        int toID = getVertexID(toCoords);
        float distance = (float) DistanceUtil.distTo(fromCoords, toCoords);
        float weight = (distance * 60.0f / maxSpeed);

        addEdge(name, fromID, toID, weight, distance, junction, canDrive, canBike, canWalk);

        if (!oneWay && !oneWayBike) {
            addEdge(name, toID, fromID, weight, distance, junction, canDrive, canBike, canWalk);

        } else if (!oneWay && oneWayBike) {
            addEdge(name, toID, fromID, weight, distance, junction, canDrive, false, canWalk);

        } else if (!oneWayBike && oneWay) {
            addEdge(name, toID, fromID, weight, distance, junction, false, canBike, canWalk);
        }
    }

    /**
     * Creates a new Edge between two vertices fromID and toID.
     * Edge is added to the list of adjacent edges of each Vertex.
     * <p>
     * Resize the array of Edges if necessary.
     */
    public void addEdge(String name,
                        int fromID,
                        int toID,
                        float weight,
                        float distance,
                        boolean junction,
                        boolean canDrive,
                        boolean canBike,
                        boolean canWalk) {

        Edge edge = new Edge(name, fromID, toID, weight, distance, junction, canDrive, canBike, canWalk);

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

    /**
     * @return list of adjacent Edges to a vertex with the given id.
     */
    public List<Edge> getAdjacentEdges(int vertexID) {
        List<Edge> edgeList = new ArrayList<>();

        Vertex vertex = vertices[vertexID];
        if (vertex != null) {
            for (int id : vertex.getAdjacentEdges()) {
                Edge edge = edges[id];
                if (edge != null) {
                    edgeList.add(edge);
                }
            }
        }
        return edgeList;
    }

    /**
     * @return out degree of vertex with given vertexID.
     * <p>
     * Used to count the amount of possible exits in a roundabout.
     * <p>
     * The out degree is only increased for edges where driving is enabled.
     * This is to ensure it properly works for oneways in roundabouts.
     */
    public int getOutDegree(int vertexID) {
        List<Edge> edges = getAdjacentEdges(vertexID);
        int outDegree = 0;

        for (Edge edge : edges) {
            if (edge.getFrom() == vertexID) {
                if (edge.canDrive()) {
                    outDegree++;
                }
            }
        }
        return outDegree;
    }

    /**
     * Calculate the bearing for an edge.
     * <p>
     * This code was written by Jonas Lindvig, all credit goes to him and his group.
     */
    private float calculateBearing(Edge edge) {
        Vertex fromVertex = vertices[edge.getFrom()];
        Vertex toVertex = vertices[edge.getTo()];

        float[] fromCoords = fromVertex.getCoords();
        float[] toCoords = toVertex.getCoords();

        float lat1Radian = (float) Math.toRadians(fromCoords[0]);
        float lon1Radian = (float) Math.toRadians(-fromCoords[1] * 0.56f); //Use "real" coordinates

        float lat2Radian = (float) Math.toRadians(toCoords[0]);
        float lon2Radian = (float) Math.toRadians(-toCoords[1] * 0.56f); //Use "real" coordinates

        float deltaLon = lon2Radian - lon1Radian;

        float y = (float) (Math.sin(deltaLon) * Math.cos(lat2Radian));
        float x = (float) (Math.cos(lat1Radian) * Math.sin(lat2Radian) - (Math.sin(lat1Radian)
                * Math.cos(lat2Radian) * Math.cos(deltaLon)));

        float bearing = (float) Math.atan2(y, x);
        bearing = (float) (((bearing * 180) / Math.PI + 360) % 360);
        bearing = 360 - bearing; // count degrees counter-clockwise - remove to make clockwise
        return bearing;
    }

    /**
     * Determine the direction between 2 edges.
     * <p>
     * If both edges have the same name return Direction.STRAIGHT
     * Otherwise calculate the bearing for the two edges to determine the direction.
     * <p>
     * This code was written by Jonas Lindvig, all credit goes to him and his group.
     */
    public Direction getDirectionFromBearing(Edge before, Edge after) {
        if (before.getName() != null && after.getName() != null) {
            if (before.getName().equals(after.getName())) {
                return Direction.STRAIGHT;
            }
        }
        float angle;
        Direction output = Direction.STRAIGHT;

        float bearingBefore = calculateBearing(before);
        float bearingAfter = calculateBearing(after);

        if (bearingBefore > bearingAfter) {
            angle = 360 - (Math.abs(bearingAfter - bearingBefore));
        } else {
            angle = bearingAfter - bearingBefore;
        }
        if (angle >= 45.0f && angle <= 180.0f) {
            return Direction.TURN_RIGHT;

        } else if (angle >= 180.0f && angle <= 315.0f) {
            return Direction.TURN_LEFT;
        }
        return output;
    }

    public int getVertexAmount() {
        return vertexAmount;
    }

    public int getEdgeAmount() {
        return edgeAmount;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public Edge getEdge(int id) {
        return edges[id];
    }
}
