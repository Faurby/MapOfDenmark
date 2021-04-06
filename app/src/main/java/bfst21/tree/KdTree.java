package bfst21.tree;

import bfst21.vector.osm.Way;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class KdTree implements Serializable {

    private static final long serialVersionUID = -7974247333826441763L;
    private KdNode root;
    private int depth = 0;

    public KdNode getRoot() {
        return root;
    }

    public List<Way> preRangeSearch(BoundingBox boundingBox) {
        depth = 0;

        List<Way> list = new ArrayList<>();
        rangeSearch(boundingBox, root, list);

        return list;
    }

    public void rangeSearch(BoundingBox boundingBox, KdNode kdNode, List<Way> list) {
        if (kdNode != null) {

            if (kdNode.getList() != null) {
                for (Way way : kdNode.getList()) {
                    if (boundingBox.intersects(way.getBoundingBox())) {
                        list.add(way);
                    }
                }
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
                    checkRight = true;

                } else if (maxY < y) {
                    checkLeft = true;
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

    public void build(List<Way> wayList) {
        depth = 0;

        if (wayList.size() > 0) {
            wayList.sort(Comparator.comparingDouble(Way::getX));
            int median = wayList.size() / 2;

            Way medianWay = wayList.get(median);
            root = new KdNode(medianWay.getX(), medianWay.getY());

            List<Way> leftList = new ArrayList<>(wayList.subList(0, median));
            List<Way> rightList = new ArrayList<>(wayList.subList(median, wayList.size()));

            depth++;
            addChild(root, leftList, false);
            addChild(root, rightList, true);
        }
    }

    public void displayTree(KdNode node, boolean right) {
        if (node != null) {
            float x = node.getX();
            float y = node.getY();

            int elements = 0;
            if (node.getList() != null) {
                elements = node.getList().size();
            }

            System.out.println(depth + " - (" + x + "," + y + ") Right: " + right + " elements: " + elements);
            System.out.println("-----------------------");

            depth++;
            displayTree(node.getRightChild(), true);
            depth--;
            depth++;
            displayTree(node.getLeftChild(), false);
            depth--;
        }
    }

    public void addChild(KdNode currentElement, List<Way> list, boolean right) {

        if (list != null) {
            if (list.size() > 0) {

                if (depth % 2 == 0) {
                    list.sort(Comparator.comparingDouble(Way::getX));
                } else {
                    list.sort(Comparator.comparingDouble(Way::getY));
                }
                int median = list.size() / 2;
                Way medianWay = list.get(median);

                KdNode medianNode = new KdNode(medianWay.getX(), medianWay.getY());
                if (right) {
                    currentElement.setRightChild(medianNode);
                } else {
                    currentElement.setLeftChild(medianNode);
                }
                if (list.size() <= 3) {
                    medianNode.setList(list);

                } else {
                    List<Way> leftList = new ArrayList<>(list.subList(0, median));
                    List<Way> rightList = new ArrayList<>(list.subList(median, list.size()));

                    depth++;
                    addChild(medianNode, leftList, false);
                    addChild(medianNode, rightList, true);
                    depth--;
                }
            }
        }
    }
}
