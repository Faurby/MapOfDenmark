package bfst21.test;

import bfst21.pathfinding.Dijkstra;
import bfst21.pathfinding.DirectedGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class DirectedGraphTest {

    @Test
    public void test() {
        DirectedGraph directedGraph = new DirectedGraph();

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 1};
        float[] coords2 = new float[]{2, 2};
        float[] coords3 = new float[]{2, 3};
        float[] coords4 = new float[]{3, 2};
        float[] coords5 = new float[]{3, 3};

        directedGraph.createVertex(coords0, 0);
        directedGraph.createVertex(coords1, 1);
        directedGraph.createVertex(coords2, 2);
        directedGraph.createVertex(coords3, 3);
        directedGraph.createVertex(coords4, 4);
        directedGraph.createVertex(coords5, 5);

        directedGraph.addEdge(coords0, coords3, 10, false);
        directedGraph.addEdge(coords2, coords1, 10, false);
        directedGraph.addEdge(coords2, coords3, 10, false);
        directedGraph.addEdge(coords2, coords4, 10, false);
        directedGraph.addEdge(coords4, coords5, 10, false);

        Dijkstra dijkstra = new Dijkstra(directedGraph, coords0, coords5);
        assertTrue(dijkstra.hasPathTo(1));
        assertTrue(dijkstra.hasPathTo(2));
        assertTrue(dijkstra.hasPathTo(3));
        assertTrue(dijkstra.hasPathTo(4));
        assertTrue(dijkstra.hasPathTo(5));
    }
}
