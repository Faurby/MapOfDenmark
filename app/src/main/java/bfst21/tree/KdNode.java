package bfst21.tree;

import bfst21.vector.osm.Node;
import bfst21.vector.osm.Way;

import java.util.List;


public class KdNode {

    private KdNode rightChild;
    private KdNode leftChild;
    private Way way;

    //private float x, y;
    private BoundingBox boundingBox;

    protected void updateBoundingBox() {

        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;

        List<Node> list = way.getNodes();

        for (Node node : list) {

            float x = node.getX();
            float y = node.getY();

            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
        }
        boundingBox = new BoundingBox(maxX, minX, maxY, minY);
    }

    public KdNode(Way way) {
        this.way = way;
    }

//    public void updateMedian() {
//        int size = way.getNodes().size();
//        Node middle = way.getNodes().get(size / 2);
//
//        x = middle.getX();
//        y = middle.getY();
//    }

    public Way getWay() {
        return way;
    }

    public void setRightChild(KdNode kdNode) {
        rightChild = kdNode;
    }

    public void setLeftChild(KdNode kdNode) {
        leftChild = kdNode;
    }

    public KdNode getRightChild() {
        return rightChild;
    }

    public KdNode getLeftChild() {
        return leftChild;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
