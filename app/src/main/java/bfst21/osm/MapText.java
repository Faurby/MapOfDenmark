package bfst21.osm;


import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * MapText is used to display text on the map such as cities, towns, villages, etc...
 * <p>
 * Each MapText has a specific MapTextType which determines
 * what zoom level is required to display the text.
 * <p>
 * Extends BoundingBoxElement so it can be placed in a KD-tree.
 */
public class MapText extends BoundingBoxElement implements Drawable {

    private static final long serialVersionUID = -2287560037258388900L;

    private final String name;
    MapTextType mapTextType;

    private float[] coords;

    public MapText(String name, MapTextType mapTextType) {
        this.name = name;
        this.mapTextType = mapTextType;
    }

    public void setCoords(float[] coords) {
        this.coords = coords;
        updateBoundingBox(coords[0], coords[1]);
    }

    public String getName() {
        return name;
    }

    public MapTextType getMapTextType() {
        return mapTextType;
    }

    public float[] getCoords() {
        return coords;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {

        if (mapTextType.getZoomLevelRequired() <= zoomLevel) {

            double size = mapTextType.getFontSizeMultiplier() / zoomLevel;
            if (zoomLevel <= 200.0D) {
                size = mapTextType.getFontSizeMultiplier() / 220.0D;
            }
            gc.setFont(new Font("Calibri", size));
            gc.fillText(name, coords[0], coords[1]);
        }
    }
}
