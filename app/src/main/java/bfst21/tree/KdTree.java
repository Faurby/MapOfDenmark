package bfst21.tree;

import bfst21.models.DistanceUtil;
import bfst21.osm.BoundingBoxElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * KdTree is a 2d binary search tree.
 * <p>
 * This is heavily inspired by the standard kd-tree data structure
 * but modifications have been made to only draw certain elements on a map.
 * <p>
 * Elements inserted must extend BoundingBoxElement so its
 * bounding box can be used to build and search the kd-tree.
 */
public class KdTree<T extends BoundingBoxElement> implements Serializable {

    private static final long serialVersionUID = -7974247333826441763L;
    private KdNode<T> root;
    private int depth = 0;
    private int maxDepth = 0;

    private float[] currentNearestNeighbor = null;

    /**
     * Build the kd-tree with the given list of elements.
     */
    public void build(List<T> elementList) {
        depth = 0;

        if (elementList.size() > 3) {
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

    private void addChild(KdNode<T> currentElement, List<T> list, boolean right) {

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
                maxX = leftList.get(middle - 1).getMaxX();

                for (T element : rightList) {
                    if (element.getMinX() < minX) {
                        minX = element.getMinX();
                    }
                }
            } else {
                maxY = leftList.get(middle - 1).getMaxY();

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

    /**
     * Start a range search within the given BoundingBox of the screen.
     * The range search starts at the root KdNode.
     *
     * @return list of elements found by the range search.
     */
    public List<T> rangeSearch(BoundingBox boundingBox) {
        depth = 0;

        List<T> list = new ArrayList<>();
        rangeSearch(boundingBox, root, list);

        return list;
    }

    /**
     * Inner range search method which will recursively continue the
     * search until all elements within the BoundingBox have been found.
     * <p>
     * The BoundingBox of the current KdNode will
     * be compared with the BoundingBox of the screen.
     * This is used to determine what side of the split is checked.
     * In cases where elements are overlapping, both sides are checked.
     * <p>
     * If we reach a leaf KdNode, we add elements to
     * the list if they intersect the given BoundingBox.
     */
    private void rangeSearch(BoundingBox boundingBox, KdNode<T> kdNode, List<T> list) {
        if (kdNode != null) {

            if (kdNode.getList() != null) { //Check if KdNode is a leaf
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

            //When depth is divisible by 2, we compare x-values instead of y-values.
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

    /**
     * Start a nearest neighbor search with the given query coords.
     * The nearest neighbor search starts at the root KdNode.
     *
     * @return coords of the nearest neighbor.
     */
    public float[] nearestNeighborSearch(float[] queryCoords) {
        depth = 0;

        nearestNeighborSearch(queryCoords, root);
        return currentNearestNeighbor;
    }

    /**
     * Inner nearest neighbor search method which will continue
     * recursively until the nearest neighbor has been found.
     */
    private void nearestNeighborSearch(float[] queryCoords, KdNode<T> kdNode) {
        boolean isLeaf = kdNode.getList() != null;

        boolean checkRight = false;

        if (isLeaf) {
            investigateLeaf(queryCoords, kdNode);

        } else {
            if (depth % 2 == 0) {
                float nodeMinX = kdNode.getMinX();
                double x = queryCoords[0];

                if (nodeMinX <= x) {
                    checkRight = true;
                }
            } else {
                float nodeMinY = kdNode.getMinY();
                double y = queryCoords[1];

                if (nodeMinY <= y) {
                    checkRight = true;
                }
            }
            depth++;
            if (checkRight) {
                nearestNeighborSearch(queryCoords, kdNode.getRightChild());
                investigateOtherSide(queryCoords, kdNode.getLeftChild());
            } else {
                nearestNeighborSearch(queryCoords, kdNode.getLeftChild());
                investigateOtherSide(queryCoords, kdNode.getRightChild());
            }
            depth--;
        }
    }

    /**
     * Investigate leaf KdNode to update current nearest neighbor.
     */
    private void investigateLeaf(float[] queryCoords, KdNode<T> kdNode) {
        double distanceToCurrentNeighbor;

        if (currentNearestNeighbor != null) {
            distanceToCurrentNeighbor = DistanceUtil.distTo(queryCoords, currentNearestNeighbor);
        } else {
            distanceToCurrentNeighbor = Double.POSITIVE_INFINITY;
        }

        for (T element : kdNode.getList()) {

            float[] coords = element.getCoords();

            for (int i = 0; i < coords.length; i += 2) {
                float x = coords[i];
                float y = coords[i + 1];

                double distance = DistanceUtil.distTo(queryCoords[0], queryCoords[1], x, y);

                if (currentNearestNeighbor != null) {
                    distanceToCurrentNeighbor = DistanceUtil.distTo(queryCoords, currentNearestNeighbor);
                }
                if (distance < distanceToCurrentNeighbor) {
                    currentNearestNeighbor = new float[]{x, y};
                }
            }
        }
    }

    /**
     * Investigate the other side of a split to see if there
     * are elements closer than the current nearest neighbor.
     * <p>
     * If anything is found, a nearest neighbor search
     * is called to check the other side of the split.
     */
    private void investigateOtherSide(float[] queryCoords, KdNode<T> kdNode) {
        double distanceToCurrentNeighbor = DistanceUtil.distTo(queryCoords, currentNearestNeighbor);

        if (depth % 2 == 0) {
            float nodeMaxX = kdNode.getMaxX();
            float nodeMinX = kdNode.getMinX();
            float x = queryCoords[0];

            float distToMax = Math.abs(x - nodeMaxX);
            float distToMin = Math.abs(x - nodeMinX);

            if (distanceToCurrentNeighbor > Math.min(distToMax, distToMin)) {
                nearestNeighborSearch(queryCoords, kdNode);
            }

        } else {
            float nodeMaxY = kdNode.getMaxY();
            float nodeMinY = kdNode.getMinY();
            float y = queryCoords[1];

            float distToMax = Math.abs(y - nodeMaxY);
            float distToMin = Math.abs(y - nodeMinY);

            if (distanceToCurrentNeighbor > Math.min(distToMax, distToMin)) {
                nearestNeighborSearch(queryCoords, kdNode);
            }
        }
    }

    /**
     * @return list of all elements in the kd-tree.
     * This is only used for unit testing.
     */
    public List<T> getAllElements() {
        List<T> list = new ArrayList<>();

        getAllElements(root, list);
        return list;
    }

    private void getAllElements(KdNode<T> kdNode, List<T> list) {
        if (kdNode != null) {
            if (kdNode.getList() != null) {
                list.addAll(kdNode.getList());

            } else {
                getAllElements(kdNode.getLeftChild(), list);
                getAllElements(kdNode.getRightChild(), list);
            }
        }
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
