package bfst21.osm;

import java.util.List;


/**
 * BoundingBoxElement is an abstract class used for classes with a bounding box.
 * <p>
 * The bounding box is updated every time a new coordinate is added.
 * This is to increase drawing performance as the values are calculated
 * while parsing OSM data instead of when navigating the map.
 */
public abstract class BoundingBoxElement extends Element {

    private static final long serialVersionUID = 8229695993958002260L;

    protected boolean initialBoundingBoxUpdate = true;
    protected float minX, maxX, minY, maxY;

    protected float[] coords = new float[2];

    public BoundingBoxElement(long id) {
        super(id);
    }

    /**
     * Create the coords array with the given list of Nodes.
     * The coords array is double the size of the Nodes array as we need a spot for each x and y.
     * <p>
     * The bounding box values are updated with the given Node coordinates.
     */
    public void setNodes(List<Node> nodes) {
        coords = new float[nodes.size() * 2];

        int count = 0;
        for (Node node : nodes) {
            float nX = node.getX();
            float nY = node.getY();

            coords[count] = nX;
            coords[count + 1] = nY;
            count += 2;

            updateBoundingBox(nX, nY);
        }
    }

    /**
     * Update the bounding box values of this element with the given coordinates.
     * The bounding box is calculated by finding the smallest and largest x,y values.
     * <p>
     * The initial values are set if this is the first time the values are being updated.
     */
    protected void updateBoundingBox(float nX, float nY) {
        if (initialBoundingBoxUpdate) {
            initialBoundingBoxUpdate = false;

            minX = nX;
            maxX = nX;
            minY = nY;
            maxY = nY;

        } else {
            if (nX < minX) {
                minX = nX;
            }
            if (nY < minY) {
                minY = nY;
            }
            if (nX > maxX) {
                maxX = nX;
            }
            if (nY > maxY) {
                maxY = nY;
            }
        }
    }

    public abstract float[] getCoords();

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
