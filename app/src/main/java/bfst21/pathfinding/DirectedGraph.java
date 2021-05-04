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

    private int vertexAmount, edgeAmount;

    /**
     * Clean up vertices and edges by removing unused slots in the array.
     * We count how many elements are actually present, then create a
     * new array with the correct size and copy all elements over.
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
            String name,
            float[] fromCoords,
            float[] toCoords,
            int maxSpeed,
            boolean oneWay,
            boolean oneWayBike,
            boolean canDrive,
            boolean canBike,
            boolean canWalk) {

        int fromID = getVertexID(fromCoords);
        int toID = getVertexID(toCoords);
        float distance = (float) Util.distTo(fromCoords, toCoords);
        float weight = (distance * 60.0f / maxSpeed);

        addEdge(name, fromID, toID, weight, distance, canDrive, canBike, canWalk);

        if (!oneWay && !oneWayBike) {
            addEdge(name, toID, fromID, weight, distance, canDrive, canBike, canWalk);

        } else if (!oneWay) {
            addEdge(name, toID, fromID, weight, distance, canDrive, false, canWalk);
            
        } else if (!oneWayBike) {
            addEdge(name, toID, fromID, weight, distance, false, canBike, canWalk);
        }
    }

    public void addEdge(
            String name,
            int fromID,
            int toID,
            float weight,
            float distance,
            boolean canDrive,
            boolean canBike,
            boolean canWalk) {

        Edge edge = new Edge(name, fromID, toID, weight, distance, canDrive, canBike, canWalk);

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
                if (edge != null) {
                    edgeList.add(edge);
                }
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

    public float[] getVector(Edge edge) {
        Vertex fromVertex = vertices[edge.getFrom()];
        Vertex toVertex = vertices[edge.getTo()];

        float[] fromCoords = fromVertex.getCoords();
        float[] toCoords = toVertex.getCoords();

        return new float[]{fromCoords[0] - toCoords[0], fromCoords[1] - toCoords[1]};
    }

    public Direction getDirectionRightLeft(Edge before, Edge after) {

        float[] beforeVector = getVector(before);
        float[] afterVector = getVector(after);

        Direction beforeDirection = getDirection(beforeVector);
        Direction afterDirection = getDirection(afterVector);

        if (beforeDirection == Direction.NORTH_WEST) {
            if (afterDirection == Direction.NORTH_EAST) {
                return Direction.TURN_LEFT;

            } else if (afterDirection == Direction.SOUTH_WEST) {
                return Direction.TURN_RIGHT;
            }
        } else if (beforeDirection == Direction.NORTH_EAST) {
            if (afterDirection == Direction.SOUTH_EAST) {
                return Direction.TURN_LEFT;

            } else if (afterDirection == Direction.NORTH_WEST) {
                return Direction.TURN_RIGHT;
            }
        } else if (beforeDirection == Direction.SOUTH_EAST) {
            if (afterDirection == Direction.SOUTH_WEST) {
                return Direction.TURN_LEFT;

            } else if (afterDirection == Direction.NORTH_EAST) {
                return Direction.TURN_RIGHT;
            }
        } else if (beforeDirection == Direction.SOUTH_WEST) {
            if (afterDirection == Direction.NORTH_WEST) {
                return Direction.TURN_LEFT;

            } else if (afterDirection == Direction.SOUTH_EAST) {
                return Direction.TURN_RIGHT;
            }
        }
        return Direction.STRAIGHT;
    }

    private Direction getDirection(float[] vector) {
        if (vector[0] == 0 && vector[1] > 0) {
            return Direction.NORTH;
        }
        if (vector[0] < 0 && vector[1] == 0) {
            return Direction.WEST;
        }
        if (vector[0] == 0 && vector[1] < 0) {
            return Direction.SOUTH;
        }
        if (vector[0] > 0 && vector[1] == 0) {
            return Direction.EAST;
        }
        if (vector[0] > 0 && vector[1] > 0) {
            return Direction.NORTH_EAST;
        }
        if (vector[0] > 0 && vector[1] < 0) {
            return Direction.SOUTH_EAST;
        }
        if (vector[0] < 0 && vector[1] > 0) {
            return Direction.NORTH_WEST;
        }
        if (vector[0] < 0 && vector[1] < 0) {
            return Direction.SOUTH_WEST;
        }
        return Direction.UNKNOWN;
    }
}
