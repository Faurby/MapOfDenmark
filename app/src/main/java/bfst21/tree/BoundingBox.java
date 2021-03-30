package bfst21.tree;


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
