package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;


/**
 * UserNode is a point of interest selected by the user.
 * Every UserNode has a name, description and coordinates.
 */
public class UserNode extends Node implements Drawable, Serializable {

    private static final long serialVersionUID = -5801814520475467424L;

    private String name;
    private String description;

    public UserNode(float lat, float lon, String name, String description) {
        super(lat, lon);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(getX(), getY());
        gc.lineTo(getX(), getY());
    }
}