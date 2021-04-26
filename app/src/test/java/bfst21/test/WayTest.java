package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bfst21.osm.Node;
import org.junit.jupiter.api.Test;
import bfst21.osm.Way;

import java.util.Arrays;


public class WayTest {

    @Test
    public void testCircleMerge() {
        Node node1 = new Node(1, 1);
        Node node2 = new Node(1, 2);
        Node node3 = new Node(1, 3);
        Node node4 = new Node(1, 4);

        Way way1 = new Way(1);
        way1.setNodes(Arrays.asList(node1, node2, node3));

        Way way2 = new Way(2);
        way2.setNodes(Arrays.asList(node3, node4, node1));

        Way way3 = new Way(3);
        way3.setNodes(Arrays.asList(node1, node2, node3, node4, node1));

        Way merged = Way.merge(way1, way2, false);

        float[] expectedCoords = way3.getCoords();
        float[] mergedCoords = merged.getCoords();

        for (int i = 0; i < way3.getCoords().length; i++) {
            assertEquals(expectedCoords[i], mergedCoords[i]);
        }
    }

    @Test
    public void testNullWayMerge() {
        Node node1 = new Node(1, 1);
        Node node2 = new Node(1, 2);
        Node node3 = new Node(1, 3);

        Way way1 = new Way(1);
        way1.setNodes(Arrays.asList(node1, node2, node3));

        assertEquals(way1, Way.merge(way1, null, false));
        assertEquals(way1, Way.merge(null, way1, false));
    }
}
