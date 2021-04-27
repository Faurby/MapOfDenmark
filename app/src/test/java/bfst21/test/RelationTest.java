package bfst21.test;

import bfst21.osm.Node;
import bfst21.osm.Relation;
import bfst21.osm.Way;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


public class RelationTest {

    @Test
    public void mergeRelation_correctMerge() {
        Relation rel = new Relation(1337);

        Way way1 = new Way(1);
        Way way2 = new Way(2);
        Way way3 = new Way(3);
        Way way4 = new Way(4);

        way1.setRole("outer");
        way2.setRole("outer");
        way3.setRole("outer");
        way4.setRole("outer");

        Node node1 = new Node(12.582717f, -99.42255f);
        Node node2 = new Node(12.582533f, -99.4223f);
        Node node3 = new Node(12.575992f, -99.421005f);
        Node node4 = new Node(12.575649f, -99.42077f);

        way1.setNodes(Arrays.asList(node1, node2));
        way2.setNodes(Arrays.asList(node3, node2));
        way3.setNodes(Arrays.asList(node4, node1));
        way4.setNodes(Arrays.asList(node4, node3));

        rel.addWay(way1);
        rel.addWay(way2);
        rel.addWay(way3);
        rel.addWay(way4);

        rel.setMultipolygon(true);
        rel.mergeOuterWays();

        float[] expected = new float[]{
                12.575649f, -99.42077f,
                12.582717f, -99.42255f,
                12.582533f, -99.4223f,
                12.575992f, -99.421005f,
                12.575649f, -99.42077f
        };

        float[] actual = new float[1];
        if (rel.getWays().size() > 0) {
            actual = rel.getWays().get(0).getCoords();
        }
        for (int i = 0; i < actual.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }
}