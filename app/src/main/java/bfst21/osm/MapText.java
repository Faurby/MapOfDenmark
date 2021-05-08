package bfst21.osm;


/**
 * MapText is used to display text on the map such as cities, towns, villages, etc...
 * <p>
 * Each MapText has a specific MapTextType which determines
 * what zoom level is required to display the text.
 * <p>
 * Extends BoundingBoxElement so it can be placed in a KD-tree.
 */
public class MapText extends BoundingBoxElement {

    private static final long serialVersionUID = -2287560037258388900L;

    private final String name;
    MapTextType mapTextType;

    private float[] coords;
    private float areaSize = 1.0f;

    public MapText(String name, MapTextType mapTextType) {
        this.name = name;
        this.mapTextType = mapTextType;
    }

    public void setCoords(float[] coords) {
        this.coords = coords;
        updateBoundingBox(coords[0], coords[1]);
    }

    public void setAreaSize(float areaSize) {
        this.areaSize = areaSize;
    }

    public String getName() {
        return name;
    }

    public MapTextType getMapTextType() {
        return mapTextType;
    }

    public float[] getCoords() {
        return coords;
    }

    public boolean canDraw(double zoomLevel) {
        return mapTextType.getZoomLevelRequired() <= zoomLevel;
    }
}
