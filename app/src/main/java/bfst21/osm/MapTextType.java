package bfst21.osm;


//TODO make it so municipalities and peninsula are registered correctly
// maybe give different text types different colors?
// delete peninsula and island if this information is still not used later on

/**
 * MapTextType is used to separate MapText into groups
 * depending on the place tag parsed in the given OSM data.
 * <p>
 * Each MapTextType has a required zoom level and and a font size multiplier.
 */
public enum MapTextType {

    PENINSULA(
            1D,
            40D
    ),
    CITY(
            50D,
            30D
    ),
    ISLAND(
            1_000D,
            30D
    ),
    TOWN(
            1_500D,
            20D
    ),
    ISLET(
            2_000D,
            0.01D
    ),
    VILLAGE(
            4_000D,
            20D
    ),
    SUBURB(
            10_000D,
            20D
    ),
    HAMLET(
            10_000D,
            20D
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
