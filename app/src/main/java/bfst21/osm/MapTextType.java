package bfst21.osm;


//TODO make it so municipalities and peninsula are registered correctly
//TODO maybe give different text types different colors?

public enum MapTextType {
    PENINSULA( //TODO delete peninsula and island if this information is still not used later on
            1,
            40D
    ),
    CITY(
            50,
            30D
    ),
    ISLAND(
            1_000,
            30D
    ),
    TOWN(
            1_500,
            20D
    ),
    ISLET(
            2_000,
            0.01D
    ),
    VILLAGE(
            4_000,
            20D
    ),
    SUBURB(
            10_000,
            20D
    ),
    HAMLET(
            10_000,
            20D
    ),
    USERNODE(
            1,
            40D
    );

    float zoomLevelRequired;
    double standardModifier;

    MapTextType(float zoomLevelRequired, double standardMultiplier) {
        this.zoomLevelRequired = zoomLevelRequired;
        this.standardModifier = standardMultiplier;
    }

    public double getStandardMultiplier() {
        return standardModifier;
    }
}
