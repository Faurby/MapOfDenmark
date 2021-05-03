package bfst21.test;

import bfst21.models.TransportOption;
import bfst21.models.TransportOptions;
import bfst21.pathfinding.DijkstraPath;
import bfst21.pathfinding.DirectedGraph;
import bfst21.pathfinding.Edge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DirectedGraphTest {

    @Test
    public void dijkstraHasPathToAllVertices() {
        DirectedGraph directedGraph = new DirectedGraph();

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 1};
        float[] coords2 = new float[]{2, 2};
        float[] coords3 = new float[]{2, 3};
        float[] coords4 = new float[]{3, 2};
        float[] coords5 = new float[]{3, 3};
        float[] coords6 = new float[]{9, 9};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);
        directedGraph.createVertex(coords6);

        directedGraph.addEdge(null, coords0, coords3, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords1, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords4, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 10, false, false, true, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        assertTrue(dijkstraPath.hasPathTo(1));
        assertTrue(dijkstraPath.hasPathTo(2));
        assertTrue(dijkstraPath.hasPathTo(3));
        assertTrue(dijkstraPath.hasPathTo(4));
        assertTrue(dijkstraPath.hasPathTo(5));

        assertFalse(dijkstraPath.hasPathTo(6));
    }

    @Test
    public void dijkstraPathTo_withOneWay_hasCorrectPath(){
        DirectedGraph directedGraph = new DirectedGraph();

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 2};
        float[] coords2 = new float[]{3, 2};
        float[] coords3 = new float[]{3, 1};
        float[] coords4 = new float[]{4, 1};
        float[] coords5 = new float[]{4, 2};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);

        directedGraph.addEdge(null, coords0, coords1, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords1, coords2, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords3, coords4, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords5, coords2, 10, true, false, true, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        int counter = 0;
        for (Edge ignored : dijkstraPath.pathTo(5)) {
            counter++;
        }
        boolean actualEdge = counter == 5;

        assertTrue(actualEdge);
    }

    @Test
    public void dijkstraPathTo_withDifferentWeights_hasCorrectPath(){
        DirectedGraph directedGraph = new DirectedGraph();

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 2};
        float[] coords2 = new float[]{3, 2};
        float[] coords3 = new float[]{3, 1};
        float[] coords4 = new float[]{4, 1};
        float[] coords5 = new float[]{4, 2};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);

        directedGraph.addEdge(null, coords0, coords1, 100, false, false, true, true, true);
        directedGraph.addEdge(null, coords1, coords2, 100, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 100, false, false, true, true, true);
        directedGraph.addEdge(null, coords3, coords4, 100, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 100, false, false, true, true, true);
        directedGraph.addEdge(null, coords5, coords2, 10, false, false, true, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        int counter = 0;
        for (Edge ignored : dijkstraPath.pathTo(5)) {
            counter++;
        }
        boolean actualEdge = counter == 5;

        assertTrue(actualEdge);
    }

    @Test
    public void dijkstraPathTo_withBikeAndWalkPath_hasCorrectPath() {
        DirectedGraph directedGraph = new DirectedGraph();
        TransportOptions.getInstance().setCurrentlyEnabled(TransportOption.CAR);

        float[] coords0 = new float[]{1, 2};
        float[] coords1 = new float[]{2, 2};
        float[] coords2 = new float[]{3, 2};
        float[] coords3 = new float[]{3, 1};
        float[] coords4 = new float[]{4, 1};
        float[] coords5 = new float[]{4, 2};

        directedGraph.createVertex(coords0);
        directedGraph.createVertex(coords1);
        directedGraph.createVertex(coords2);
        directedGraph.createVertex(coords3);
        directedGraph.createVertex(coords4);
        directedGraph.createVertex(coords5);

        directedGraph.addEdge(null, coords0, coords1, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords1, coords2, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords2, coords3, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords3, coords4, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords4, coords5, 10, false, false, true, true, true);
        directedGraph.addEdge(null, coords5, coords2, 10, false, false, false, true, true);

        DijkstraPath dijkstraPath = new DijkstraPath(directedGraph, coords0, coords5);
        int counter = 0;
        for (Edge ignored : dijkstraPath.pathTo(5)) {
            counter++;
        }
        boolean actualEdge = counter == 5;

        assertTrue(actualEdge);
    }




}
