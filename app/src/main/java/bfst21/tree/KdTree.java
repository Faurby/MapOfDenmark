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
    private int maxDepth = 0;

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

                if (nodeMaxX >= minX) {
                    checkLeft = true;
                }
                if (nodeMinX <= maxX) {
                    checkRight = true;
                }
            } else {
                float nodeMaxY = kdNode.getMaxY();
                float nodeMinY = kdNode.getMinY();
                float maxY = boundingBox.getMaxY();
                float minY = boundingBox.getMinY();

                if (nodeMaxY >= minY) {
                    checkLeft = true;
                }
                if (nodeMinY <= maxY) {
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

    public void build(List<Way> wayList) {
        depth = 0;

        if (wayList.size() > 0) {
            wayList.sort(Comparator.comparingDouble(Way::getMaxX));
            int middle = wayList.size() / 2;

            List<Way> leftList = new ArrayList<>(wayList.subList(0, middle));
            List<Way> rightList = new ArrayList<>(wayList.subList(middle, wayList.size()));

            float maxX = leftList.get(middle - 1).getMaxX();
            float minX = Float.MAX_VALUE;

            for (Way way : rightList) {
                if (way.getMinX() < minX) {
                    minX = way.getMinX();
                }
            }
            root = new KdNode(minX, maxX, Float.MIN_VALUE, Float.MAX_VALUE);

            depth++;
            addChild(root, leftList, false);
            addChild(root, rightList, true);
        }
    }

    public void addChild(KdNode currentElement, List<Way> list, boolean right) {

        if (list != null) {
            if (list.size() > 0) {

                if (depth > maxDepth) {
                    maxDepth = depth;
                }
                boolean sortX = depth % 2 == 0;
                boolean doCreateLeafNode = list.size() <= 3;

                if (sortX) {
                    list.sort(Comparator.comparingDouble(Way::getMaxX));
                } else {
                    list.sort(Comparator.comparingDouble(Way::getMaxY));
                }

                float maxX = Float.MIN_VALUE;
                float maxY = Float.MIN_VALUE;
                float minX = Float.MAX_VALUE;
                float minY = Float.MAX_VALUE;

                int middle = list.size() / 2;

                List<Way> leftList = new ArrayList<>(list.subList(0, middle));
                List<Way> rightList = new ArrayList<>(list.subList(middle, list.size()));

                if (!doCreateLeafNode) {
                    if (sortX) {
                        if (leftList.size() > 0) {
                            maxX = leftList.get(middle - 1).getMaxX();
                        }
                        for (Way way : rightList) {
                            if (way.getMinX() < minX) {
                                minX = way.getMinX();
                            }
                        }
                    } else {
                        if (leftList.size() > 0) {
                            maxY = leftList.get(middle - 1).getMaxY();
                        }
                        for (Way way : rightList) {
                            if (way.getMinY() < minY) {
                                minY = way.getMinY();
                            }
                        }
                    }
                } else {
                    Way middleWay = list.get(middle);
                    minX = middleWay.getMinX();
                    maxX = middleWay.getMaxX();
                    minY = middleWay.getMinY();
                    maxY = middleWay.getMaxY();
                }

                KdNode middleNode = new KdNode(minX, maxX, minY, maxY);
                if (right) {
                    currentElement.setRightChild(middleNode);
                } else {
                    currentElement.setLeftChild(middleNode);
                }
                if (doCreateLeafNode) {
                    middleNode.setList(list);

                } else {
                    depth++;
                    addChild(middleNode, leftList, false);
                    addChild(middleNode, rightList, true);
                    depth--;
                }
            }
        }
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
