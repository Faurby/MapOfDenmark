package bfst21.tree;

import bfst21.osm.Element;


public abstract class BoundingBoxElement extends Element {

    private static final long serialVersionUID = 8229695993958002260L;
    protected float minX, maxX, minY, maxY;

    public BoundingBoxElement(long id) {
        super(id);
    }

    public abstract float[] getCoords();

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
