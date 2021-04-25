package bfst21.test;

import bfst21.osm.Node;
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

        Node n0 = directedGraph.getVertexNode(0);
        Node n1 = directedGraph.getVertexNode(1);
        Node n2 = directedGraph.getVertexNode(2);
        Node n3 = directedGraph.getVertexNode(3);
        Node n4 = directedGraph.getVertexNode(4);
        Node n5 = directedGraph.getVertexNode(5);

        directedGraph.addEdge(n0, n2, 10, false);
        directedGraph.addEdge(n2, n1, 10, false);
        directedGraph.addEdge(n2, n3, 10, false);
        directedGraph.addEdge(n2, n4, 10, false);
        directedGraph.addEdge(n4, n5, 10, false);

        Dijkstra dijkstra = new Dijkstra(directedGraph, n0, n5);
        assertTrue(dijkstra.hasPathTo(1));
        assertTrue(dijkstra.hasPathTo(2));
        assertTrue(dijkstra.hasPathTo(3));
        assertTrue(dijkstra.hasPathTo(4));
        assertTrue(dijkstra.hasPathTo(5));
    }
}
