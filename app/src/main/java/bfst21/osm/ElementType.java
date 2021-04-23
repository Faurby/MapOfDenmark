package bfst21.osm;

import bfst21.models.DisplayOption;
import bfst21.models.DisplayOptions;
import javafx.scene.paint.Color;


public enum ElementType {

    //It is very important that each ElementType is declared in the correct drawing order
    //For example, we do not want to draw BUILDING below ISLAND
    ISLAND(
            1f,
            0.0f,
            0.0D,
            Color.rgb(223, 222, 222),
            Color.rgb(151, 151, 151),
            Color.rgb(211, 211, 211)
    ),
    LANDUSE(
            1f,
            500.0f,
            0.0D,
            Color.rgb(172, 220, 180),
            Color.rgb(69, 80, 7),
            Color.rgb(211, 211, 211)
    ),
    WATER(
            1f,
            500.0f,
            0.0D,
            Color.rgb(160, 196, 252),
            Color.rgb(0, 126, 170),
            Color.rgb(0, 0, 0)
    ),
    WATERWAY(
            0.0002f,
            10000.0f,
            0.0D,
            Color.rgb(160, 196, 252),
            Color.rgb(0, 126, 170),
            Color.rgb(0, 0, 0)
    ),
    CYCLEWAY(
            0.0001f,
            50000.0f,
            0.0001D,
            Color.rgb(33, 33, 250),
            Color.rgb(33, 33, 250),
            Color.rgb(33, 33, 250)
    ),
    FOOTWAY(
            0.0001f,
            50000.0f,
            0.0001D,
            Color.rgb(252, 132, 116),
            Color.rgb(252, 132, 116),
            Color.rgb(252, 132, 116)
    ),
    PEDESTRIAN(
            0.0001f,
            50000.0f,
            0.0D,
            Color.rgb(223, 241, 242),
            Color.rgb(223, 241, 242),
            Color.rgb(223, 241, 242)
    ),
    ROAD(
            0.0003f,
            50000.0f,
            0.0D,
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255)
    ),
    RESIDENTIAL(
            0.00015f,
            10000.0f,
            0.0D,
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255)
    ),
    TRUNK(
            0.0003f,
            50000.0f,
            0.0D,
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255)
    ),
    MOTORWAY(
            0.0003f,
            500.0f,
            0.0D,
            Color.rgb(248, 197, 81),
            Color.rgb(248, 198, 81),
            Color.rgb(169, 169, 169)
    ),
    TERTIARY(
            0.0003f,
            3000.0f,
            0.0D,
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 255)
    ),
    PRIMARY(
            0.0003f,
            1500.0f,
            0.0D,
            Color.rgb(236, 148, 164),
            Color.rgb(255, 140, 0),
            Color.rgb(255, 255, 255)
    ),
    BUILDING(
            1f,
            40000.0f,
            0.0D,
            Color.rgb(197, 185, 175),
            Color.rgb(51, 51, 51),
            Color.rgb(169, 169, 169)
    ),
    UNKNOWN(
            0.002f,
            500.0f,
            0.0D,
            Color.rgb(255, 0, 0),
            Color.rgb(255, 0, 0),
            Color.rgb(255, 0, 0)
    );

    private final Color color, colorBlind, blackWhite;
    private final float drawSize, zoomLevelRequired;
    private final double lineDashes;

    ElementType(float drawSize, float zoomLevelRequired, double lineDashes, Color color, Color colorBlind, Color blackWhite) {
        this.color = color;
        this.colorBlind = colorBlind;
        this.blackWhite = blackWhite;
        this.drawSize = drawSize;
        this.lineDashes = lineDashes;
        this.zoomLevelRequired = zoomLevelRequired;
    }

    /**
     * Determine if this ElementType should be using the fill drawing method.
     */
    public boolean doFillDraw() {
        return this == ElementType.BUILDING ||
               this == ElementType.ISLAND ||
               this == ElementType.LANDUSE ||
               this == ElementType.WATER;
    }

    /**
     * Determine if there should be created multiple trees for this ElementType.
     * A tree will be created for each ElementSize with this ElementType.
     */
    public boolean hasMultipleSizes() {
        return this == ElementType.BUILDING ||
                this == ElementType.LANDUSE ||
                this == ElementType.WATER;
    }

    public boolean canNavigate() {
        return this == ElementType.PRIMARY ||
                this == ElementType.MOTORWAY ||
                this == ElementType.TRUNK ||
                this == ElementType.TERTIARY ||
                this == ElementType.CYCLEWAY ||
                this == ElementType.RESIDENTIAL ||
                this == ElementType.ROAD ||
                this == ElementType.FOOTWAY;
    }

    public boolean canDrive() {
        return this == ElementType.PRIMARY ||
                this == ElementType.MOTORWAY ||
                this == ElementType.RESIDENTIAL ||
                this == ElementType.TERTIARY ||
                this == ElementType.TRUNK;
    }

    public boolean canBike() {
        return this == ElementType.TERTIARY ||
                this == ElementType.CYCLEWAY ||
                this == ElementType.ROAD ||
                this == ElementType.RESIDENTIAL;
    }

    public boolean canWalk() {
        return this == ElementType.TERTIARY ||
                this == ElementType.CYCLEWAY ||
                this == ElementType.RESIDENTIAL ||
                this == ElementType.ROAD ||
                this == ElementType.FOOTWAY;
    }

    public boolean isDisplayOptionEnabled() {
        return DisplayOptions.getInstance().getBool(DisplayOption.valueOf("DISPLAY_" + this));
    }

    public double getLineDashes() {
        return lineDashes;
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

    public boolean doShowElement(double zoomLevel) {
        return zoomLevel >= zoomLevelRequired;
    }

    public float getZoomLevelRequired() {
        return zoomLevelRequired;
    }
}