package bfst21.pathfinding;

import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import edu.princeton.cs.algs4.IndexMinPQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * DijkstraPath is based on the Dijkstra algorithm.
 * It used to find the shortest path between the
 * origin coordinates and the destination coordinates.
 * <p>
 * The algorithm will stop once the destination has been located.
 */
public class DijkstraPath {

    private final double[] distTo;
    private final Edge[] edgeTo;
    private final IndexMinPQ<Double> pq;

    private final boolean isDriving;
    private boolean foundDestination;

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
        distTo[sourceID] = 0.0;

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

    public boolean hasPathTo(int targetID) {
        return distTo[targetID] < Double.POSITIVE_INFINITY;
    }

    public List<Edge> pathTo(int targetID) {
        if (!hasPathTo(targetID)) {
            return null;
        }
        List<Edge> path = new ArrayList<>();
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
