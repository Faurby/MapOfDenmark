package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import bfst21.osm.Node;
import bfst21.osm.Way;

public class WayTest {
    @Test
    public void testCircleMerge() {
        Node n1 = new Node(1, 1);
        Node n2 = new Node(1, 2);
        Node n3 = new Node(1, 3);
        Node n4 = new Node(1, 4);
        Node n5 = new Node(1, 5);
        Node n6 = new Node(1, 6);
        Way w1 = new Way(1);
            w1.add(n1);
            w1.add(n2);
            w1.add(n3);
        Way w2 = new Way(2);
            w2.add(n3);
            w2.add(n4);
            w2.add(n1);
        Way w3 = new Way(3);
            w3.add(n1);
            w3.add(n2);
            w3.add(n3);
            w3.add(n4);
            w3.add(n1);
        assertEquals(w3, Way.merge(w1, w2));
    }

    @Test
    public void testNullWayMerge() {
        Node n1 = new Node(1, 1);
        Node n2 = new Node(1, 2);
        Node n3 = new Node(1, 3);
        Way w1 = new Way(1);
            w1.add(n1);
            w1.add(n2);
            w1.add(n3);
        assertEquals(w1, Way.merge(w1, null));
        assertEquals(w1, Way.merge(null, w1));
    }
}
