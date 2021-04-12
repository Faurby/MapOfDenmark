package bfst21.osm;

import javafx.scene.paint.Color;


public enum WayType {

    BUILDING(1f, 40000.0f, Color.rgb(197,185,175), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    ISLAND(1f, 0.0f, Color.rgb(223,222,222), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    LANDUSE(1f, 500.0f, Color.rgb(172,220,180), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    MOTORWAY(0.0004f, 500.0f, Color.rgb(248,197,81), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    RESIDENTIAL(0.0002f, 10000.0f, Color.rgb(255,255,255), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    TERTIARY(0.0004f, 3000.0f, Color.rgb(255,255,255), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    PRIMARY(0.0004f, 1500.0f, Color.rgb(236,148,164), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    WATER(1f, 500.0f, Color.rgb(160,196,252), Color.rgb(1, 1,1), Color.rgb(1, 1,1)),
    WATERWAY(0.0002f, 10000.0f, Color.rgb(160,196,252), Color.rgb(1, 1,1), Color.rgb(1, 1,1));

    private Color color;
    private Color colorBlind;
    private Color blackWhite;
    private float drawSize;
    private float zoomLevelRequired;

    WayType(float drawSize, float zoomLevelRequired, Color color, Color colorBlind, Color blackWhite) {
        this.color = color;
        this.colorBlind = colorBlind;
        this.blackWhite = blackWhite;
        this.drawSize = drawSize;
        this.zoomLevelRequired = zoomLevelRequired;
    }

    public Color getColor() {
        return color;
    }

    public Color getColorBlind() {
        return colorBlind;
    }

    public Color getBlackWhite() {
        return blackWhite;
    }

    public float getDrawSize() {
        return drawSize;
    }

    public float getZoomLevelRequired() {
        return zoomLevelRequired;
    }
}