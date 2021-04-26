package bfst21.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import bfst21.osm.Way;


public class WayTest {

    @Test
    public void testCircleMerge() {
        float[] coords1 = new float[]{1, 1};
        float[] coords2 = new float[]{1, 2};
        float[] coords3 = new float[]{1, 3};
        float[] coords4 = new float[]{1, 4};

        Way way1 = new Way(1);
        way1.addNode(coords1);
        way1.addNode(coords2);
        way1.addNode(coords3);

        Way way2 = new Way(2);
        way2.addNode(coords3);
        way2.addNode(coords4);
        way2.addNode(coords1);

        Way way3 = new Way(3);
        way3.addNode(coords1);
        way3.addNode(coords2);
        way3.addNode(coords3);
        way3.addNode(coords4);
        way3.addNode(coords1);

        Way merged = Way.merge(way1, way2, false);

        float[] expectedCoords = way3.getCoords();
        float[] mergedCoords = merged.getCoords();

        for (int i = 0; i < way3.getCoordsAmount(); i++) {
            assertEquals(expectedCoords[i], mergedCoords[i]);
        }
    }

    @Test
    public void testNullWayMerge() {
        float[] coords1 = new float[]{1, 1};
        float[] coords2 = new float[]{1, 2};
        float[] coords3 = new float[]{1, 3};

        Way w1 = new Way(1);
        w1.addNode(coords1);
        w1.addNode(coords2);
        w1.addNode(coords3);

        assertEquals(w1, Way.merge(w1, null, false));
        assertEquals(w1, Way.merge(null, w1, false));
    }
}
