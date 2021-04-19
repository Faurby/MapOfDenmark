package bfst21.pathfinding;

import edu.princeton.cs.algs4.IndexMinPQ;


public class Dijkstra {

    private double[] distTo;
    private Edge[] edgeTo;
    private IndexMinPQ<Double> pq;

    public Dijkstra(DirectedGraph directedGraph, Vertex source) {

        int vertexAmount = directedGraph.getVertexAmount();

        distTo = new double[vertexAmount];
        edgeTo = new Edge[vertexAmount];

        //validateVertex(s);

        int sourceID = source.getID();

        for (int v = 0; v < vertexAmount; v++) {
            distTo[v] = Double.POSITIVE_INFINITY;
        }
        distTo[sourceID] = 0.0;

        pq = new IndexMinPQ<>(vertexAmount);
        pq.insert(sourceID, distTo[sourceID]);
        while (!pq.isEmpty()) {
            int vertexID = pq.delMin();
            Vertex vertex = directedGraph.getVertex(vertexID);
            if (vertex != null) {
                for (Edge edge : vertex.getEdges()) {
                    relax(edge);
                }
            }
        }
    }

    public void relax(Edge edge) {
//        Vertex from = edge.getFrom();
//        Vertex to = edge.getTo();
//
//        double weight = edge.getWeight();
//
//        int v = from.getID();
//        int w = to.getID();
//
//        if (distTo[w] > distTo[v] + weight) {
//            distTo[w] = distTo[v] + weight;
//            edgeTo[w] = edge;
//
//            if (pq.contains(w)) {
//                pq.decreaseKey(w, distTo[w]);
//
//            } else {
//                pq.insert(w, distTo[w]);
//            }
//        }
    }
}
