package bfst21.osm;

/**
 * MapTextType is used to separate MapText into groups
 * depending on the place tag parsed in the given OSM data.
 * <p>
 * Each MapTextType has a required zoom level and and a font size multiplier.
 */
public enum MapTextType {

    CITY(
            50.0D,
            30.0D
    ),
    TOWN(
            1_500.0D,
            20.0D
    ),
    VILLAGE(
            4_000.0D,
            20.0D
    ),
    SUBURB(
            10_000.0D,
            20.0D
    ),
    HAMLET(
            10_000.0D,
            20.0D
    );

    private final double zoomLevelRequired;
    private final double fontSizeMultiplier;

    MapTextType(double zoomLevelRequired, double fontSizeMultiplier) {
        this.zoomLevelRequired = zoomLevelRequired;
        this.fontSizeMultiplier = fontSizeMultiplier;
    }

    public double getFontSizeMultiplier() {
        return fontSizeMultiplier;
    }

    public double getZoomLevelRequired() {
        return zoomLevelRequired;
    }
}
