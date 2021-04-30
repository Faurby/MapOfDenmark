package bfst21.osm;


//TODO make it so municipalities and peninsula are registered correctly
//TODO maybe give different text types different colors?

public enum MapTextType {
    PENINSULA(
1
    ),
    CITY(
50
    ),
    ISLAND(
1_000
    ),
    TOWN(
1_000
    ),
    VILLAGE(
2_500
    ),
    ISLET(
2_000
    ),
    SUBURB(
12_000
    ),
    HAMLET(
12_000
    );

    float zoomLevelRequired;

    MapTextType(float zoomLevelRequired){
        this.zoomLevelRequired = zoomLevelRequired;
    }



}
