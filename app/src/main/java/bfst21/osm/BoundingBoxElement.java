package bfst21.osm;

public abstract class BoundingBoxElement extends Element {

    private static final long serialVersionUID = 8229695993958002260L;
    protected float minX, maxX, minY, maxY;

    public BoundingBoxElement(long id) {
        super(id);
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
