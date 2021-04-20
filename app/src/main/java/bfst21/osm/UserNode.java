package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;


public class UserNode extends Node implements Drawable {

    private static final long serialVersionUID = -5801814520475467424L;
    private String description;

    public UserNode(float lat, float lon, String description) {
        super(lat, lon);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void changeDescription(String newDesc) {
        description = newDesc;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(getX(), getY());
        gc.lineTo(getX(), getY());
    }
}