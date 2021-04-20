package bfst21.pathfinding;

import java.io.Serializable;
import java.util.Objects;


public class VertexPoint implements Serializable {

    private static final long serialVersionUID = 2577330911650138815L;
    private final float x, y;

    public VertexPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexPoint that = (VertexPoint) o;
        return Float.compare(that.x, x) == 0 && Float.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
