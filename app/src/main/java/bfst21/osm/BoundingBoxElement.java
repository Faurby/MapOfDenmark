package bfst21.osm;

/**
 * BoundingBoxElement is an abstract class used for classes with a bounding box.
 *
 * The bounding box is updated every time a new coordinate is added.
 * This is to increase drawing performance as the values are calculated
 * while parsing OSM data instead of when navigating the map.
 */
public abstract class BoundingBoxElement extends Element {

    private static final long serialVersionUID = 8229695993958002260L;

    protected boolean initialBoundingBoxUpdate = true;
    protected float minX, maxX, minY, maxY;

    protected float[] coords = new float[2];
    protected int coordsAmount = 0;

    public BoundingBoxElement(long id) {
        super(id);
    }

    /**
     * Add the coordinates of a node to the coords array.
     * Resize the coords array if necessary.
     * Update bounding box values.
     */
    public void addNode(float[] nodeCoords) {
        if (coordsAmount == coords.length) {
            coords = resizeArray(coords, coordsAmount, coords.length * 2);
        }
        coords[coordsAmount] = nodeCoords[0];
        coords[coordsAmount + 1] = nodeCoords[1];
        coordsAmount += 2;

        updateBoundingBox(nodeCoords, initialBoundingBoxUpdate);
        initialBoundingBoxUpdate = false;
    }

    /**
     * Resize an input array to the new size given.
     * Copy all the previous values over to the new array.
     *
     * Some arrays may have a lot of empty slots, so the old
     * size value is used to only iterate through valid values.
     */
    protected float[] resizeArray(float[] input, int oldSize, int newSize) {
        float[] copy = new float[newSize];

        for (int i = 0; i < oldSize; i++) {
            copy[i] = input[i];
        }
        return copy;
    }

    protected void updateBoundingBox(float[] coords, boolean initialBoundingBoxUpdate) {
        float nX = coords[0];
        float nY = coords[1];

        if (initialBoundingBoxUpdate) {
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
