package bfst21.osm;


public class MapText extends BoundingBoxElement {

    private static final long serialVersionUID = -2287560037258388900L;

    private final String name;
    private final String place;

    private float[] coords;

    public MapText(String name, String place) {
        this.name = name;
        this.place = place;
    }

    public void setCoords(float[] coords) {
        this.coords = coords;
        updateBoundingBox(coords[0], coords[1]);
    }

    public String getName() {
        return name;
    }

    public String getPlace() {
        return place;
    }

    public float[] getCoords() {
        return coords;
    }

    public boolean canDraw(double zoomLevel) {
        if (zoomLevel >= 500 && place.equals("island")) {
            return true;

        } else if (zoomLevel >= 1_000 && place.equals("city")) {
            return true;

        } else if (zoomLevel >= 2_000 && place.equals("islet")) {
            return true;

        } else if (zoomLevel >= 3_500 && place.equals("municipality")) {
            return true;

        } else if (zoomLevel >= 3_500 && place.equals("town")) {
            return true;

        } else if (zoomLevel >= 6_000 && place.equals("village")) {
            return true;

        } else if (zoomLevel >= 12_000 && place.equals("suburb")) {
            return true;

        } else return zoomLevel >= 12_000 && place.equals("hamlet");
    }
}
