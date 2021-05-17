package bfst21.osm;

import bfst21.models.TransportOption;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


/**
 * Way is used to organize and construct MapWays for MapData.
 * <p>
 * Way is mostly used while parsing and building MapData.
 * When pre-construction is complete, MapWay is used for drawing elements on the map.
 * This decreases memory usage as we don't need specific fields after constructing MapData.
 */
public class Way implements Serializable {

    private static final long serialVersionUID = -1621640480144599897L;

    private ElementType elementType;
    private String role;
    private String name;
    private boolean junction;
    private boolean oneWay;
    private boolean oneWayBike;
    private int maxSpeed = 30;
    private final MapWay mapWay = new MapWay();

    private final HashMap<TransportOption, Boolean> transportOptions = new HashMap<>();

    /**
     * Calculate and return the ElementSize of Way by getting the area of its bounding box.
     */
    public ElementSize getElementSize() {
        if (elementType.hasMultipleSizes()) {
            double xLength = Math.abs(mapWay.maxX - mapWay.minX);
            double yLength = Math.abs(mapWay.maxY - mapWay.minY);
            double areaSize = (xLength * yLength * Math.pow(10.0D, 9.0D));
            return ElementSize.getSize(areaSize);
        }
        return ElementSize.DEFAULT;
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
        int firstSize = first.mapWay.coords.length;
        int secondSize = second.mapWay.coords.length;
        int mergedSize = firstSize + secondSize - 2;
        //mergedSize needs to be 2 less because we are removing a common node when merging.

        if (reverse) {
            second.mapWay.coords = reverseCoordsArray(second.mapWay.coords);
        }

        Way merged = new Way();
        merged.mapWay.coords = new float[mergedSize];

        for (int i = 0; i < first.mapWay.coords.length; i++) {
            merged.mapWay.coords[i] = first.mapWay.coords[i];
        }
        for (int i = 2; i < second.mapWay.coords.length; i++) {
            int position = i - 2 + firstSize;
            merged.mapWay.coords[position] = second.mapWay.coords[i];
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

    public boolean canNavigate(TransportOption transportOption) {
        if (transportOptions.containsKey(transportOption)) {
            return transportOptions.get(transportOption);
        }
        return elementType.canNavigate(transportOption);
    }

    public void setTransportOption(TransportOption transportOption, boolean allowed) {
        transportOptions.put(transportOption, allowed);
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

    public Node first() {
        return new Node(mapWay.coords[0], mapWay.coords[1]);
    }

    public Node last() {
        return new Node(mapWay.coords[mapWay.coords.length - 2], mapWay.coords[mapWay.coords.length - 1]);
    }

    public MapWay getMapWay() {
        return mapWay;
    }

    public void setNodes(List<Node> nodes) {
        mapWay.setNodes(nodes);
    }
}
