package bfst21.tree;

import bfst21.osm.Way;

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

                    float minX = way.getMinX();
                    float maxX = way.getMaxX();
                    float minY = way.getMinY();
                    float maxY = way.getMaxY();

                    if (boundingBox.intersects(maxX, maxY, minX, minY)) {
                        list.add(way);
                    }
                }
            }
            boolean checkRight = false;
            boolean checkLeft = false;

            if (depth % 2 == 0) {
                float nodeMaxX = kdNode.getMaxX();
                float nodeMinX = kdNode.getMinX();
                float maxX = boundingBox.getMaxX();
                float minX = boundingBox.getMinX();

                if (nodeMaxX >= maxX) { //View bounding box is to the left of the split
                    checkLeft = true;
                } else if (nodeMinX <= minX) { //View bounding box is to the right of the split
                    checkRight = true;
                } else if ((maxX >= nodeMinX && minX <= nodeMinX) // View bounding box intersects kMinX
                        || (maxX >= nodeMaxX && minX <= nodeMaxX) // View bounding box intersects kMaxX
                        || (minX >= nodeMinX && maxX <= nodeMaxX) // View bounding box is between kMinX and kMaxX
                        || (minX <= nodeMinX && maxX >= nodeMaxX)) { // View bounding box intersects both kMinX and kMaxX
                    checkRight = true;
                    checkLeft = true;
                }
            } else {
                float nodeMaxY = kdNode.getMaxY();
                float nodeMinY = kdNode.getMinY();
                float maxY = boundingBox.getMaxY();
                float minY = boundingBox.getMinY();

                if (nodeMaxY >= maxY) { //View bounding box is to the left of the split
                    checkLeft = true;
                } else if (nodeMinY <= minY) { //View bounding box is to the right of the split
                    checkRight = true;
                } else if ((maxY >= nodeMinY && minY <= nodeMinY) // View bounding box intersects kMinY
                        || (maxY >= nodeMaxY && minY <= nodeMaxY) // View bounding box intersects kMaxY
                        || (minY >= nodeMinY && maxY <= nodeMaxY) // View bounding box is between kMinY and kMaxY
                        || (minY <= nodeMinY && maxY >= nodeMaxY)) { // View bounding box intersects both kMinY and kMaxY
                    checkRight = true;
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
            wayList.sort(Comparator.comparingDouble(Way::getMaxX));
            int median = wayList.size() / 2;

            Way medianWay = wayList.get(median);
            float maxX = medianWay.getMaxX();
            wayList.sort(Comparator.comparingDouble(Way::getMinX));
            medianWay = wayList.get(median);
            float minX = medianWay.getMinX();

            root = new KdNode(minX, maxX, Float.MIN_VALUE, Float.MAX_VALUE);

            List<Way> leftList = new ArrayList<>(wayList.subList(0, median));
            List<Way> rightList = new ArrayList<>(wayList.subList(median, wayList.size()));

            depth++;
            addChild(root, leftList, false);
            addChild(root, rightList, true);
        }
    }

    public void addChild(KdNode currentElement, List<Way> list, boolean right) {

        if (list != null) {
            if (list.size() > 0) {

                boolean sortX = depth % 2 == 0;

                if (sortX) {
                    list.sort(Comparator.comparingDouble(Way::getMaxX));
                } else {
                    list.sort(Comparator.comparingDouble(Way::getMaxY));
                }
                int median = list.size() / 2;
                Way medianWay = list.get(median);
                float maxX = medianWay.getMaxX();
                float maxY = medianWay.getMaxY();

                if (sortX) {
                    list.sort(Comparator.comparingDouble(Way::getMinX));
                } else {
                    list.sort(Comparator.comparingDouble(Way::getMinY));
                }
                medianWay = list.get(median);
                float minX = medianWay.getMinX();
                float minY = medianWay.getMinY();

                if (sortX) {
                    minY = Float.MIN_VALUE;
                    maxY = Float.MAX_VALUE;
                } else {
                    minX = Float.MIN_VALUE;
                    maxX = Float.MAX_VALUE;
                }

                KdNode medianNode = new KdNode(minX, maxX, minY, maxY);
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
