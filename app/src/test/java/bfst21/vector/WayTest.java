package bfst21.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import bfst21.vector.osm.Node;
import bfst21.vector.osm.Way;

public class WayTest {
    @Test
    public void testCircleMerge() {
        var n1 = new Node(1,1,1);
        var n2 = new Node(2,2,1);
        var n3 = new Node(3,3,1);
        var n4 = new Node(4,4,1);
        var n5 = new Node(5,5,1);
        var n6 = new Node(6,6,1);
        var w1 = new Way();
        w1.add(n1);
        w1.add(n2);
        w1.add(n3);
        var w2 = new Way();
        w2.add(n3);
        w2.add(n4);
        w2.add(n1);
        var w3 = new Way();
        w3.add(n1);
        w3.add(n2);
        w3.add(n3);
        w3.add(n4);
        w3.add(n1);
        assertEquals(w3, Way.merge(w1, w2));
    }

    @Test
    public void testNullWayMerge() {
        var n1 = new Node(1,1,1);
        var n2 = new Node(2,2,1);
        var n3 = new Node(3,3,1);
        var w1 = new Way();
        w1.add(n1);
        w1.add(n2);
        w1.add(n3);
        assertEquals(w1, Way.merge(w1, null));
        assertEquals(w1, Way.merge(null, w1));
    }
}
