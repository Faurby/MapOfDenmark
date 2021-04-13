package bfst21.test;

import bfst21.tree.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BoundingBoxTest {

    @Test
    public void testBoxInsideBox() {
        BoundingBox inside = new BoundingBox(2, 3, 2, 3);
        BoundingBox outside = new BoundingBox(1, 4, 1, 4);

        assertTrue(inside.intersects(outside));
        assertTrue(outside.intersects(inside));
    }

    @Test
    public void testBoxNotIntersecting() {
        BoundingBox bb1 = new BoundingBox(2, 3, 2, 3);
        BoundingBox bb2 = new BoundingBox(10, 20, 10, 20);

        assertFalse(bb1.intersects(bb2));
        assertFalse(bb2.intersects(bb1));
    }

    @Test
    public void theMediumRareTest() {
        BoundingBox bb1 = new BoundingBox(1, 6, 1, 3);
        BoundingBox bb2 = new BoundingBox(1, 3, 1, 6);
        BoundingBox bb3 = new BoundingBox(4, 6, 1, 6);
        BoundingBox bb4 = new BoundingBox(1, 6, 4, 6);
        BoundingBox bbBig = new BoundingBox(2, 5, 2, 5);

        assertTrue(bb1.intersects(bbBig));
        assertTrue(bb2.intersects(bbBig));
        assertTrue(bb3.intersects(bbBig));
        assertTrue(bb4.intersects(bbBig));

        assertTrue(bbBig.intersects(bb1));
        assertTrue(bbBig.intersects(bb2));
        assertTrue(bbBig.intersects(bb3));
        assertTrue(bbBig.intersects(bb4));
    }

    @Test
    public void theBigTest() {
        BoundingBox bb1 = new BoundingBox(1, 3, 1, 3);
        BoundingBox bb2 = new BoundingBox(4, 5, 1, 3);
        BoundingBox bb3 = new BoundingBox(6, 8, 1, 3);
        BoundingBox bb4 = new BoundingBox(1, 3, 4, 5);
        BoundingBox bb5 = new BoundingBox(6, 7, 4, 5);
        BoundingBox bb6 = new BoundingBox(1, 3, 6, 8);
        BoundingBox bb7 = new BoundingBox(4, 5, 6, 8);
        BoundingBox bb8 = new BoundingBox(6, 8, 6, 8);
        BoundingBox bbBig = new BoundingBox(2, 7, 2, 7);

        assertTrue(bb1.intersects(bbBig));
        assertTrue(bb2.intersects(bbBig));
        assertTrue(bb3.intersects(bbBig));
        assertTrue(bb4.intersects(bbBig));
        assertTrue(bb5.intersects(bbBig));
        assertTrue(bb6.intersects(bbBig));
        assertTrue(bb7.intersects(bbBig));
        assertTrue(bb8.intersects(bbBig));

        assertTrue(bbBig.intersects(bb1));
        assertTrue(bbBig.intersects(bb2));
        assertTrue(bbBig.intersects(bb3));
        assertTrue(bbBig.intersects(bb4));
        assertTrue(bbBig.intersects(bb5));
        assertTrue(bbBig.intersects(bb6));
        assertTrue(bbBig.intersects(bb7));
        assertTrue(bbBig.intersects(bb8));
    }
}
