package bfst21.osm;


public abstract class BoundingBoxElement extends Element {

    private static final long serialVersionUID = 8229695993958002260L;
    protected float minX, maxX, minY, maxY;

    protected float[] coords = new float[2];
    protected int coordsAmount = 0;
    private boolean initialBoundingBoxUpdate = true;

    public BoundingBoxElement(long id) {
        super(id);
    }

    public void addNode(float[] nodeCoords) {
        if (coordsAmount == coords.length) {
            resizeCoords(coords.length * 2);
        }
        coords[coordsAmount] = nodeCoords[0];
        coords[coordsAmount + 1] = nodeCoords[1];
        coordsAmount += 2;

        updateBoundingBox(nodeCoords, initialBoundingBoxUpdate);
        initialBoundingBoxUpdate = false;
    }

    protected void resizeCoords(int capacity) {
        float[] copy = new float[capacity];
        for (int i = 0; i < coordsAmount; i++) {
            copy[i] = coords[i];
        }
        coords = copy;
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
