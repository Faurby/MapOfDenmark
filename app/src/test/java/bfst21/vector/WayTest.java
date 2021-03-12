package bfst21.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import bfst21.vector.osm.Node;
import bfst21.vector.osm.Way;

public class WayTest {
    @Test
    public void testCircleMerge() {
        Node n1 = new Node(1,1,1);
        Node n2 = new Node(2,2,1);
        Node n3 = new Node(3,3,1);
        Node n4 = new Node(4,4,1);
        Node n5 = new Node(5,5,1);
        Node n6 = new Node(6,6,1);
        Way w1 = new Way();
            w1.add(n1);
            w1.add(n2);
            w1.add(n3);
        Way w2 = new Way();
            w2.add(n3);
            w2.add(n4);
            w2.add(n1);
        Way w3 = new Way();
            w3.add(n1);
            w3.add(n2);
            w3.add(n3);
            w3.add(n4);
            w3.add(n1);
        assertEquals(w3, Way.merge(w1, w2));
    }

    @Test
    public void testNullWayMerge() {
        Node n1 = new Node(1,1,1);
        Node n2 = new Node(2,2,1);
        Node n3 = new Node(3,3,1);
        Way w1 = new Way();
            w1.add(n1);
            w1.add(n2);
            w1.add(n3);
        assertEquals(w1, Way.merge(w1, null));
        assertEquals(w1, Way.merge(null, w1));
    }
}
