package bfst21.test;

import bfst21.osm.Relation;
import bfst21.osm.Way;
import org.junit.jupiter.api.Test;

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

        float[] coords1 = new float[]{12.582717f, -99.42255f};
        float[] coords2 = new float[]{12.582533f, -99.4223f};
        float[] coords3 = new float[]{12.575992f, -99.421005f};
        float[] coords4 = new float[]{12.575649f, -99.42077f};

        way1.add(coords1);
        way1.add(coords2);

        way2.add(coords3);
        way2.add(coords2);

        way3.add(coords4);
        way3.add(coords1);

        way4.add(coords4);
        way4.add(coords3);

        rel.addMember(way1);
        rel.addMember(way2);
        rel.addMember(way3);
        rel.addMember(way4);

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
