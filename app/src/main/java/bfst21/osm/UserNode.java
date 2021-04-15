package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;


public class UserNode extends Node implements Drawable {

    private final String description;

    public UserNode(float lat, float lon, String description) {
        super(lat, lon);
        this.description = description;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(getX(), getY());
        gc.lineTo(getX(), getY());
    }
}