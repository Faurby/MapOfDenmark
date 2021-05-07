package bfst21.osm;

import java.io.Serializable;
import java.util.Arrays;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;


public class Way extends BoundingBoxElement implements Drawable, Serializable {

    private static final long serialVersionUID = 3139576893143362100L;

    private ElementType elementType;
    private String role;
    private int maxSpeed = 1;
    private boolean junction;
    private boolean oneWay;
    private boolean oneWayBike;
    private String name;

    /**
     * Calculate and return the ElementSize of Way by getting the area of its bounding box.
     */
    public ElementSize getElementSize() {
        if (elementType.hasMultipleSizes()) {
            double xLength = Math.abs(maxX - minX);
            double yLength = Math.abs(maxY - minY);
            double areaSize = (xLength * yLength * Math.pow(10.0D, 9.0D));
            return ElementSize.getSize(areaSize);
        }
        return ElementSize.DEFAULT;
    }

    /**
     * Draw a Way by iterating through all the coordinates.
     * At certain zoom levels, nodes may be skipped to increase drawing performance.
     * <p>
     * To avoid incorrect drawings, the first and last coordinate
     * will always be drawn, no matter the amount of nodes to skip.
     */
    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(coords[0], coords[1]);

        int nodeSkipAmount = getNodeSkipAmount(zoomLevel);
        int size = coords.length;

        for (int i = 2; i < (size - 2); i += 2 * nodeSkipAmount) {
            gc.lineTo(coords[i], coords[i + 1]);
        }
        gc.lineTo(coords[size - 2], coords[size - 1]);
    }

    /**
     * Merge the coordinates of two Ways.
     * A Way may have the same first coordinate as the last coordinate of another Way.
     * <p>
     * In that case it makes sense to merge them for ElementTypes
     * that needs to be drawn using the fill method.
     * <p>
     * Some Relations have Ways with coordinates in the wrong order,
     * so we need to reverse the list of coordinates before correctly merging.
     */
    public static Way merge(Way first, Way second, boolean reverse) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        int firstSize = first.coords.length;
        int secondSize = second.coords.length;
        int mergedSize = firstSize + secondSize - 2;
        //mergedSize needs to be 2 less because we are removing a common node when merging.

        if (reverse) {
            second.coords = reverseCoordsArray(second.coords);
        }

        Way merged = new Way();
        merged.coords = new float[mergedSize];

        for (int i = 0; i < first.coords.length; i++) {
            merged.coords[i] = first.coords[i];
        }
        for (int i = 2; i < second.coords.length; i++) {
            int position = i - 2 + firstSize;
            merged.coords[position] = second.coords[i];
        }
        return merged;
    }

    public static Way merge(Way before, Way coast, Way after) {
        return merge(merge(before, coast, false), after, false);
    }

    /**
     * Reverse an array of coordinates.
     * The input coordinates are alternately positioned in the array: x1, y1, x2, y2, etc...
     * So x1 is at index 0 and y1 is at index 1 and so on.
     * <p>
     * The output array contains the coordinate pairs in the correct reverse order.
     */
    private static float[] reverseCoordsArray(float[] input) {
        int size = input.length;
        float[] reversed = new float[size];

        int count = size;
        for (int i = 0; i < size; i += 2) {
            reversed[i] = input[count - 2];
            reversed[i + 1] = input[count - 1];
            count -= 2;
        }
        return reversed;
    }

    public static int getNodeSkipAmount(double zoomLevel) {
        if (zoomLevel <= 100) {
            return 10;
        } else if (zoomLevel <= 140) {
            return 9;
        } else if (zoomLevel <= 190) {
            return 8;
        } else if (zoomLevel <= 270) {
            return 7;
        } else if (zoomLevel <= 350) {
            return 6;
        } else if (zoomLevel <= 500) {
            return 5;
        } else if (zoomLevel <= 700) {
            return 4;
        } else if (zoomLevel <= 950) {
            return 3;
        } else if (zoomLevel <= 1350) {
            return 2;
        }
        return 1;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public void setOneWayBike(boolean oneWayBike) {
        this.oneWayBike = oneWayBike;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public boolean isOneWayBike() {
        return oneWayBike;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public ElementType getType() {
        return elementType;
    }

    public void setType(ElementType elementType) {
        this.elementType = elementType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setJunction(boolean junction) {
        this.junction = junction;
    }

    public boolean isJunction() {
        return junction;
    }

    public String getRole() {
        return role;
    }

    public float[] getCoords() {
        return coords;
    }

    public Node first() {
        return new Node(coords[0], coords[1]);
    }

    public Node last() {
        return new Node(coords[coords.length - 2], coords[coords.length - 1]);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coords == null) ? 0 : Arrays.hashCode(coords));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Way other = (Way) obj;
        if (coords == null) {
            return other.coords == null;
        } else {
            return Arrays.equals(coords, other.coords);
        }
    }
}
