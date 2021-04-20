package bfst21.tree;

import bfst21.osm.Element;
import bfst21.osm.Node;

import java.util.List;

public abstract class BoundingBoxElement extends Element {

    private static final long serialVersionUID = 8229695993958002260L;
    protected float minX, maxX, minY, maxY;

    public BoundingBoxElement(long id) {
        super(id);
    }

    public abstract List<Node> getNodes();

    protected void updateBoundingBox(Node node, boolean initialBoundingBoxUpdate) {
        if (initialBoundingBoxUpdate) {
            minX = node.getX();
            maxX = node.getX();
            minY = node.getY();
            maxY = node.getY();

        } else {
            float nX = node.getX();
            float nY = node.getY();

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

    public BoundingBox getBoundingBox() {
        return new BoundingBox(minX, maxX, minY, maxY);
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
