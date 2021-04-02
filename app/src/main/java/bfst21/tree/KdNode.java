package bfst21.tree;

import bfst21.vector.osm.Way;
import java.util.List;


public class KdNode {

    private KdNode rightChild;
    private KdNode leftChild;

    private float x, y;

    private List<Way> list;

    public KdNode(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public List<Way> getList() {
        return list;
    }

    public void setList(List<Way> list) {
        this.list = list;
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
