package bfst21.tree;


/**
 * BoundingBox represents the bounding box of the screen.
 * This is used to start a kd-tree range search within a bounding box.
 */
public class BoundingBox {

    private final float maxX, maxY, minX, minY;

    public BoundingBox(float minX, float maxX, float minY, float maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
    }

    public boolean intersects(float otherMaxX, float otherMaxY, float otherMinX, float otherMinY) {

        //Check if other box is inside this box
        if (otherMinX >= minX && otherMaxX <= maxX && otherMinY >= minY && otherMaxY <= maxY) {
            return true;

            //Check if this box is inside other box
        } else if (otherMinX <= minX && otherMaxX >= maxX && otherMinY <= minY && otherMaxY >= maxY) {
            return true;

            //Check if other min/max x is inside this box
        } else if ((otherMinX >= minX && otherMinX <= maxX) || (otherMaxX >= minX && otherMaxX <= maxX)) {
            //Check if other min/max y is inside this box
            if ((otherMinY >= minY && otherMinY <= maxY) || (otherMaxY >= minY && otherMaxY <= maxY)) {
                return true;

            } else if ((otherMinY <= minY && otherMaxY >= maxY)) {
                return true;
            }
        }
        if ((otherMinY >= minY && otherMinY <= maxY) || (otherMaxY >= minY && otherMaxY <= maxY)) {
            return otherMinX <= minX && otherMaxX >= maxX;
        }
        return false;
    }

    public boolean intersects(BoundingBox boundingBox) {
        float otherMaxX = boundingBox.getMaxX();
        float otherMaxY = boundingBox.getMaxY();
        float otherMinX = boundingBox.getMinX();
        float otherMinY = boundingBox.getMinY();

        return intersects(otherMaxX, otherMaxY, otherMinX, otherMinY);
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }
}
