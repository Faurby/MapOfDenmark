package bfst21.test;

import bfst21.pathfinding.DijkstraPath;
import bfst21.pathfinding.DirectedGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        float[] coords6 = new float[]{9, 9};

        directedGraph.createVertex(coords0, 0);
        directedGraph.createVertex(coords1, 1);
        directedGraph.createVertex(coords2, 2);
        directedGraph.createVertex(coords3, 3);
        directedGraph.createVertex(coords4, 4);
        directedGraph.createVertex(coords5, 5);
        directedGraph.createVertex(coords6, 6);

        directedGraph.addEdge(coords0, coords3, 10, false, true, true, true);
        directedGraph.addEdge(coords2, coords1, 10, false, true, true, true);
        directedGraph.addEdge(coords2, coords3, 10, false, true, true, true);
        directedGraph.addEdge(coords2, coords4, 10, false, true, true, true);
        directedGraph.addEdge(coords4, coords5, 10, false, true, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        assertTrue(dijkstraPath.hasPathTo(1));
        assertTrue(dijkstraPath.hasPathTo(2));
        assertTrue(dijkstraPath.hasPathTo(3));
        assertTrue(dijkstraPath.hasPathTo(4));
        assertTrue(dijkstraPath.hasPathTo(5));

        assertFalse(dijkstraPath.hasPathTo(6));
    }
}
