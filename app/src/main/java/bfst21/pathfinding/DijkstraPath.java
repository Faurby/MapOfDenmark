package bfst21.pathfinding;

import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import edu.princeton.cs.algs4.IndexMinPQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * DijkstraPath is based on the DijkstraSP class from
 * the algs4 library by Robert Sedgewick and Kevin Wayne.
 * <p>
 * It used to find the shortest path between the
 * origin coordinates and the destination coordinates.
 * The algorithm will stop once the destination has been located.
 * <p>
 * Weight is normally the distance of an Edge but if you are driving,
 * it will use the weight calculated using max speed and distance of a Way.
 */
public class DijkstraPath {

    private final double[] distTo;
    private final Edge[] edgeTo;
    private final IndexMinPQ<Double> pq;

    private final boolean isDriving;
    private boolean foundDestination;

    /**
     * Start Dijkstra pathfinding from the origin point.
     * Finds all the shortest paths in the graph from the origin point.
     * Stops when the destination point has been found.
     */
    public DijkstraPath(DirectedGraph directedGraph,
                        float[] originCoords,
                        float[] destinationCoords) {

        int sourceID = directedGraph.getVertexID(originCoords);
        int destinationID = directedGraph.getVertexID(destinationCoords);

        int vertexAmount = directedGraph.getVertexAmount();

        isDriving = TransportOptions.getInstance().getCurrentlyEnabled() == TransportOption.CAR;

        distTo = new double[vertexAmount];
        edgeTo = new Edge[vertexAmount];

        for (int v = 0; v < vertexAmount; v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[sourceID] = 0.0D;

        pq = new IndexMinPQ<>(vertexAmount);
        pq.insert(sourceID, distTo[sourceID]);
        while (!pq.isEmpty() && !foundDestination) {

            int vertexID = pq.delMin();

            if (vertexID == destinationID) {
                foundDestination = true;
            }
            for (Edge edge : directedGraph.getAdjacentEdges(vertexID)) {
                relax(edge);
            }
        }
    }

    /**
     * Relax an edge if navigation is possible with the currently enabled TransportOption.
     * If driving, the weight is set to the Edge weight calculated using distance and max speed.
     * If walking or biking, the weight is set to the distance of the Edge.
     */
    public void relax(Edge edge) {
        if (edge.canNavigate()) {
            int v = edge.getFrom();
            int w = edge.getTo();

            //Use the distance as weight if traveling by foot or bike.
            double weight = edge.getDistance();

            //Use the weight calculated by maxspeed and distance if we are traveling by car.
            if (isDriving) {
                weight = edge.getWeight();
            }

            if (distTo[w] > distTo[v] + weight) {
                distTo[w] = distTo[v] + weight;
                edgeTo[w] = edge;

                if (pq.contains(w)) {
                    pq.decreaseKey(w, distTo[w]);
                } else {
                    pq.insert(w, distTo[w]);
                }
            }
        }
    }

    /**
     * Check if Dijkstra has found a path between
     * the origin point and the specific target point.
     */
    public boolean hasPathTo(int targetID) {
        return distTo[targetID] < Double.POSITIVE_INFINITY;
    }

    /**
     * @return path of Edges from the origin point to the specific target point.
     */
    public List<Edge> pathTo(int targetID) {
        List<Edge> path = new ArrayList<>();

        if (!hasPathTo(targetID)) {
            return path;
        }
        for (Edge e = edgeTo[targetID]; e != null; e = edgeTo[e.getFrom()]) {
            path.add(e);
        }
        Collections.reverse(path);
        return path;
    }

    public Edge[] getEdgeTo() {
        return edgeTo;
    }
}
