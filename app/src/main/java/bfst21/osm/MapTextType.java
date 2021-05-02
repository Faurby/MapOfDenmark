package bfst21.osm;


//TODO make it so municipalities and peninsula are registered correctly
//TODO maybe give different text types different colors?

public enum MapTextType {
    PENINSULA(
            1,
            40D
    ),
    CITY(
            50,
            40D
    ),
    ISLAND(
            1_000,
            40D
    ),
    TOWN(
            1_000,
            20D
    ),
    VILLAGE(
            2_000,
            20D
    ),
    ISLET(
            2_000,
            0.01D
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
