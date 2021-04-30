package bfst21.osm;


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

    public MapTextType getPlace() {
        return mapTextType;
    }

    public float[] getCoords() {
        return coords;
    }

    public boolean canDraw(double zoomLevel) {
        return mapTextType.zoomLevelRequired <= zoomLevel;
    }
}
