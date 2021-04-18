package bfst21.tree;

import bfst21.osm.BoundingBoxElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class KdTree<T extends BoundingBoxElement> implements Serializable {

    private static final long serialVersionUID = -7974247333826441763L;
    private KdNode<T> root;
    private int depth = 0;
    private int maxDepth = 0;

    public KdNode<T> getRoot() {
        return root;
    }

    public List<T> preRangeSearch(BoundingBox boundingBox) {
        depth = 0;

        List<T> list = new ArrayList<>();
        rangeSearch(boundingBox, root, list);

        return list;
    }

    public void rangeSearch(BoundingBox boundingBox, KdNode<T> kdNode, List<T> list) {
        if (kdNode != null) {

            if (kdNode.getList() != null) {
                for (T element : kdNode.getList()) {

                    float minX = element.getMinX();
                    float maxX = element.getMaxX();
                    float minY = element.getMinY();
                    float maxY = element.getMaxY();

                    if (boundingBox.intersects(maxX, maxY, minX, minY)) {
                        list.add(element);
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

    public void build(List<T> elementList) {
        depth = 0;

        if (elementList.size() > 0) {
            elementList.sort(Comparator.comparingDouble(T::getMaxX));
            int middle = elementList.size() / 2;

            List<T> leftList = new ArrayList<>(elementList.subList(0, middle));
            List<T> rightList = new ArrayList<>(elementList.subList(middle, elementList.size()));

            float maxX = leftList.get(middle - 1).getMaxX();
            float minX = Float.MAX_VALUE;

            for (T element : rightList) {
                if (element.getMinX() < minX) {
                    minX = element.getMinX();
                }
            }
            root = new KdNode<>(minX, maxX, Float.MIN_VALUE, Float.MAX_VALUE);

            depth++;
            addChild(root, leftList, false);
            addChild(root, rightList, true);
        }
    }

    public void addChild(KdNode<T> currentElement, List<T> list, boolean right) {

        if (list != null) {
            if (list.size() > 0) {

                if (depth > maxDepth) {
                    maxDepth = depth;
                }
                boolean sortX = depth % 2 == 0;
                boolean doCreateLeafNode = list.size() <= 3;

                if (sortX) {
                    list.sort(Comparator.comparingDouble(T::getMaxX));
                } else {
                    list.sort(Comparator.comparingDouble(T::getMaxY));
                }

                float maxX = Float.MIN_VALUE;
                float maxY = Float.MIN_VALUE;
                float minX = Float.MAX_VALUE;
                float minY = Float.MAX_VALUE;

                int middle = list.size() / 2;

                List<T> leftList = new ArrayList<>(list.subList(0, middle));
                List<T> rightList = new ArrayList<>(list.subList(middle, list.size()));

                if (!doCreateLeafNode) {
                    if (sortX) {
                        if (leftList.size() > 0) {
                            maxX = leftList.get(middle - 1).getMaxX();
                        }
                        for (T element : rightList) {
                            if (element.getMinX() < minX) {
                                minX = element.getMinX();
                            }
                        }
                    } else {
                        if (leftList.size() > 0) {
                            maxY = leftList.get(middle - 1).getMaxY();
                        }
                        for (T element : rightList) {
                            if (element.getMinY() < minY) {
                                minY = element.getMinY();
                            }
                        }
                    }
                } else {
                    T middleElement = list.get(middle);
                    minX = middleElement.getMinX();
                    maxX = middleElement.getMaxX();
                    minY = middleElement.getMinY();
                    maxY = middleElement.getMaxY();
                }

                KdNode<T> middleNode = new KdNode<>(minX, maxX, minY, maxY);
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
