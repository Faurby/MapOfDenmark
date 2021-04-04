package bfst21.tree;


import java.awt.*;
import java.awt.geom.Rectangle2D;

public class BoundingBox {

    private float maxX;
    private float minX;
    private float maxY;
    private float minY;

    public BoundingBox(float maxX, float minX, float maxY, float minY) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxY = maxY;
        this.minY = minY;
    }

    public boolean intersects(BoundingBox boundingBox) {
        float otherMaxX = boundingBox.getMaxX();
        float otherMinX = boundingBox.getMinX();
        float otherMaxY = boundingBox.getMaxY();
        float otherMinY = boundingBox.getMinY();

        //Rectangle2D rect = new Rectangle2D.Float(minX, minY,maxX-minX,maxY-minY);
        //Rectangle2D otherRect = new Rectangle2D.Float(otherMinX, otherMinY,otherMaxX - otherMinX,otherMaxY - otherMinY);
        //return rect.intersects(otherRect);

        //return (minX < otherMaxX) && (otherMinX < maxX) && (minY < otherMaxY) && (minY < otherMinY && otherMinY < maxY);
        //return minX < boundingBox.maxX && maxX > boundingBox.minX && minY < boundingBox.maxY && maxY > boundingBox.minY;

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
            if ((otherMinX <= minX && otherMaxX >= maxX)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(KdNode kdNode) {
        float maxX = getMaxX();
        float minX = getMinX();
        float maxY = getMaxY();
        float minY = getMinY();

        float nodeX = kdNode.getX();
        float nodeY = kdNode.getY();

        if (maxX >= nodeX && minX <= nodeX) {
            if (maxY >= nodeY && minY <= nodeY) {
                return true;
            }
        }
        return false;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getMinY() {
        return minY;
    }
}
