package bfst21.test;

import bfst21.pathfinding.Coordinate;
import bfst21.pathfinding.Dijkstra;
import bfst21.pathfinding.DirectedGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class DirectedGraphTest {

    @Test
    public void test() {
        DirectedGraph directedGraph = new DirectedGraph();

        directedGraph.createVertex(1, 2, 0);
        directedGraph.createVertex(2, 1, 1);
        directedGraph.createVertex(2, 2, 2);
        directedGraph.createVertex(2, 3, 3);
        directedGraph.createVertex(3, 2, 4);
        directedGraph.createVertex(3, 3, 5);

        Coordinate c0 = directedGraph.getVertexCoords(0);
        Coordinate c1 = directedGraph.getVertexCoords(1);
        Coordinate c2 = directedGraph.getVertexCoords(2);
        Coordinate c3 = directedGraph.getVertexCoords(3);
        Coordinate c4 = directedGraph.getVertexCoords(4);
        Coordinate c5 = directedGraph.getVertexCoords(5);

        directedGraph.addEdge(c0, c2, 10);
        directedGraph.addEdge(c2, c1, 10);
        directedGraph.addEdge(c2, c3, 10);
        directedGraph.addEdge(c2, c4, 10);
        directedGraph.addEdge(c4, c5, 10);

        Dijkstra dijkstra = new Dijkstra(directedGraph, c0, c5);
        assertTrue(dijkstra.hasPathTo(1));
        assertTrue(dijkstra.hasPathTo(2));
        assertTrue(dijkstra.hasPathTo(3));
        assertTrue(dijkstra.hasPathTo(4));
        assertTrue(dijkstra.hasPathTo(5));
    }
}
