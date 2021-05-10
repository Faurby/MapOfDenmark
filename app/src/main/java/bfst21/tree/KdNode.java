package bfst21.tree;

import bfst21.osm.BoundingBoxElement;

import java.io.Serializable;
import java.util.List;


/**
 * KdNode is used as a node in the kd-tree.
 * <p>
 * A non-leaf KdNode has a right and left child to organize the kd-tree.
 * <p>
 * A node may be a leaf KdNode if they contain a list of elements.
 * Elements in a leaf KdNode must extend BoundingBoxElement.
 */
public class KdNode<T extends BoundingBoxElement> implements Serializable {

    private static final long serialVersionUID = 8198499209216068048L;
    private KdNode<T> rightChild, leftChild;

    private final float minX, maxX, minY, maxY;

    private List<T> list;

    public KdNode(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void setRightChild(KdNode<T> kdNode) {
        rightChild = kdNode;
    }

    public void setLeftChild(KdNode<T> kdNode) {
        leftChild = kdNode;
    }

    public KdNode<T> getRightChild() {
        return rightChild;
    }

    public KdNode<T> getLeftChild() {
        return leftChild;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }
}
