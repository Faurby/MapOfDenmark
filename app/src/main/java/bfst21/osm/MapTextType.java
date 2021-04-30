package bfst21.osm;


//TODO make it so municipalities and peninsula are registered correctly
//TODO maybe give different text types different colors?

public enum MapTextType {
    PENINSULA(
            1,
            0.02D
    ),
    CITY(
            50,
            0.02D
    ),
    ISLAND(
            1_000,
            0.02D
    ),
    TOWN(
            1_000,
            0.01D
    ),
    VILLAGE(
            2_500,
            0.01D
    ),
    ISLET(
            2_000,
            0.01D
    ),
    SUBURB(
            12_000,
            0.01D
    ),
    HAMLET(
            12_000,
            0.07D
    );

    float zoomLevelRequired;
    double standardModifier;

    MapTextType(float zoomLevelRequired, double standardMultiplier){
        this.zoomLevelRequired = zoomLevelRequired;
        this.standardModifier = standardMultiplier;
    }

    public double getStandardMultiplier(){
        return standardModifier;
    }

}
