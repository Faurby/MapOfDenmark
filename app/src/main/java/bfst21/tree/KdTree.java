package bfst21.tree;

import bfst21.vector.osm.Way;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class KdTree {

    private KdNode root;
    private List<KdNode> list;

    private int depth = 0;

    public KdTree() {
        list = new ArrayList<>();
    }

    public void add(KdNode kdNode) {
        list.add(kdNode);
    }

    public KdNode getRoot() {
        return root;
    }

    public List<KdNode> preRangeSearch(BoundingBox boundingBox) {
        depth = 0;

        List<KdNode> list = new ArrayList<>();
        rangeSearch(boundingBox, root, list);

        return list;
    }

    public void rangeSearch(BoundingBox boundingBox, KdNode kdNode, List<KdNode> list) {
        if (kdNode != null) {
            if (boundingBox.intersects(kdNode.getBoundingBox())) {
                list.add(kdNode);
            }
            boolean checkRight = false;
            boolean checkLeft = false;

            if (depth % 2 == 0) {
                float x = kdNode.getX();
                float maxX = boundingBox.getMaxX();
                float minX = boundingBox.getMinX();

                if (minX <= x && maxX >= x) {
                    checkRight = true;
                    checkLeft = true;

                } else if (minX > x) {
                    checkRight = true;

                } else if (maxX < x) {
                    checkLeft = true;
                }
            } else {
                float y = kdNode.getY();
                float maxY = boundingBox.getMaxY();
                float minY = boundingBox.getMinY();

                if (minY <= y && maxY >= y) {
                    checkRight = true;
                    checkLeft = true;

                } else if (minY > y) {
                    checkLeft = true;

                } else if (maxY < y) {
                    checkRight = true;
                }
            }
            if (checkRight) {
                depth++;
                rangeSearch(boundingBox, kdNode.getRightChild(), list);
                depth--;
            }
            if (checkLeft) {
                depth++;
                rangeSearch(boundingBox, kdNode.getLeftChild(), list);
                depth--;
            }
        }
    }

    public void preBuild() {
        depth = 0;

        list.sort(Comparator.comparingDouble(KdNode::getX));
        int median = list.size() / 2;
        root = list.get(median);

        List<KdNode> leftList = list.subList(0, median - 1);
        List<KdNode> rightList = list.subList(median + 1, list.size() - 1);

        depth++;
        addChild(root, leftList, false);
        addChild(root, rightList, true);
    }

    public void addChild(KdNode currentElement, List<KdNode> list, boolean right) {

        if (list != null) {
            if (list.size() > 0) {

                if (list.size() == 2) {
                    currentElement.setLeftChild(list.get(0));
                    currentElement.setRightChild(list.get(1));

                } else {
                    KdNode medianNode = list.get(0);
                    List<KdNode> leftList = null;
                    List<KdNode> rightList = null;

                    if (list.size() > 1) {
                        if (depth % 2 == 0) {
                            list.sort(Comparator.comparingDouble(KdNode::getX));
                        } else {
                            list.sort(Comparator.comparingDouble(KdNode::getY));
                        }
                        int median = list.size() / 2;
                        medianNode = list.get(median);

                        leftList = list.subList(0, median - 1);
                        rightList = list.subList(median + 1, list.size() - 1);
                    }

                    if (right) {
                        currentElement.setRightChild(medianNode);
                    } else {
                        currentElement.setLeftChild(medianNode);
                    }
                    if (list.size() > 1) {
                        depth++;
                        addChild(medianNode, leftList, false);
                        addChild(medianNode, rightList, true);
                    }
                }
            }
        }
    }
}
