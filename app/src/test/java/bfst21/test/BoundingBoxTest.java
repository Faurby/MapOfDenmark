package bfst21.test;

import bfst21.tree.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BoundingBoxTest {

    @Test
    public void testIntersectBoxInsideBox() {
        BoundingBox inside = new BoundingBox(2, 3, 2, 3);
        BoundingBox outside = new BoundingBox(1, 4, 1, 4);

        assertTrue(inside.intersects(outside));
        assertTrue(outside.intersects(inside));
    }

    @Test
    public void testBoxesNotIntersecting() {
        BoundingBox bb1 = new BoundingBox(2, 3, 2, 3);
        BoundingBox bb2 = new BoundingBox(10, 20, 10, 20);

        assertFalse(bb1.intersects(bb2));
        assertFalse(bb2.intersects(bb1));
    }

    @Test
    public void testIntersectBoxCoveringSideOfBox() {
        BoundingBox bbBig = new BoundingBox(2, 5, 2, 5);
        BoundingBox bb1 = new BoundingBox(1, 6, 1, 3);
        BoundingBox bb2 = new BoundingBox(1, 3, 1, 6);
        BoundingBox bb3 = new BoundingBox(4, 6, 1, 6);
        BoundingBox bb4 = new BoundingBox(1, 6, 4, 6);

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
    public void testIntersectFalseBoxBesideBox() {
        BoundingBox bb = new BoundingBox(4, 5, 4, 5);
        BoundingBox bbLesserX = new BoundingBox(1, 2, 4, 6);
        BoundingBox bbGreaterX = new BoundingBox(6, 7, 3, 5);
        BoundingBox bbLesserY = new BoundingBox(4, 7, 1, 3);
        BoundingBox bbGreaterY = new BoundingBox(1, 5, 6, 7);

        assertFalse(bbLesserX.intersects(bb));
        assertFalse(bbGreaterX.intersects(bb));
        assertFalse(bbLesserY.intersects(bb));
        assertFalse(bbGreaterY.intersects(bb));

        assertFalse(bb.intersects(bbLesserX));
        assertFalse(bb.intersects(bbGreaterX));
        assertFalse(bb.intersects(bbLesserY));
        assertFalse(bb.intersects(bbGreaterY));
    }

    @Test
    public void testIntersectCornerOrSidesOverlapping() {
        BoundingBox bbBig = new BoundingBox(2, 7, 2, 7);

        BoundingBox topLeftCorner = new BoundingBox(1, 3, 1, 3);
        BoundingBox topCenter = new BoundingBox(4, 5, 1, 3);
        BoundingBox topRightCorner = new BoundingBox(6, 8, 1, 3);
        BoundingBox centerLeft = new BoundingBox(1, 3, 4, 5);
        BoundingBox centerRight = new BoundingBox(6, 8, 4, 5);
        BoundingBox bottomLeftCorner = new BoundingBox(1, 3, 6, 8);
        BoundingBox bottomCenter = new BoundingBox(4, 5, 6, 8);
        BoundingBox bottomRightCorner = new BoundingBox(6, 8, 6, 8);

        assertTrue(topLeftCorner.intersects(bbBig));
        assertTrue(topCenter.intersects(bbBig));
        assertTrue(topRightCorner.intersects(bbBig));
        assertTrue(centerLeft.intersects(bbBig));
        assertTrue(centerRight.intersects(bbBig));
        assertTrue(bottomLeftCorner.intersects(bbBig));
        assertTrue(bottomCenter.intersects(bbBig));
        assertTrue(bottomRightCorner.intersects(bbBig));

        assertTrue(bbBig.intersects(topLeftCorner));
        assertTrue(bbBig.intersects(topCenter));
        assertTrue(bbBig.intersects(topRightCorner));
        assertTrue(bbBig.intersects(centerLeft));
        assertTrue(bbBig.intersects(centerRight));
        assertTrue(bbBig.intersects(bottomLeftCorner));
        assertTrue(bbBig.intersects(bottomCenter));
        assertTrue(bbBig.intersects(bottomRightCorner));
    }

    @Test
    public void testIntersectOnlyOneSharedEdge(){
        //Doesn't matter much whether these return true or false, as long as it's consistent
        BoundingBox bb = new BoundingBox(2, 4, 2, 4);
        BoundingBox bbShareX1 = new BoundingBox(4, 6, 2, 4);
        BoundingBox bbShareX2 = new BoundingBox(1, 2, 3, 5);
        BoundingBox bbShareY1 = new BoundingBox(2, 4, 4, 6);
        BoundingBox bbShareY2 = new BoundingBox(3, 3, 1, 2);

        assertTrue(bbShareX1.intersects(bb));
        assertTrue(bbShareX2.intersects(bb));
        assertTrue(bbShareY1.intersects(bb));
        assertTrue(bbShareY2.intersects(bb));

        assertTrue(bb.intersects(bbShareX1));
        assertTrue(bb.intersects(bbShareX2));
        assertTrue(bb.intersects(bbShareY1));
        assertTrue(bb.intersects(bbShareY2));
    }
}
