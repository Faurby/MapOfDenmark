package bfst21.test;

import bfst21.osm.Node;
import bfst21.osm.Relation;
import bfst21.osm.Way;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class RelationTest {

    @Test
    public void mergeRelation_correctMerge() {
        Relation rel = new Relation(1337);

        float[] arr1 = new float[]{1,2};
        float[] arr2 = new float[]{1,2};

        HashMap<float[], Integer> map = new HashMap<>();
        map.put(arr1, 1);

        if (map.containsKey(arr2)) {
            System.out.println("Yes");
        } else {
            System.out.println("No");
        }

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

        way1.add(node1);
        way1.add(node2);

        way2.add(node3);
        way2.add(node2);

        way3.add(node4);
        way3.add(node1);

        way4.add(node4);
        way4.add(node3);

        rel.addMember(way1);
        rel.addMember(way2);
        rel.addMember(way3);
        rel.addMember(way4);

        rel.setMultipolygon(true);
        rel.mergeOuterWays();

        List<Node> expected = Arrays.asList(node4, node1, node2, node3, node4);
        List<Node> actual = new ArrayList<>();
        if (rel.getWays().size() > 0) {
            actual = rel.getWays().get(0).getNodes();
        }
        assertEquals(expected, actual);
    }
}
