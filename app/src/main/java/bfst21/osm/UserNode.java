package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class UserNode extends Node implements Drawable{

    private final String description;

    public UserNode(float lat, float lon, String description) {
        super(lat, lon);
        this.description = description;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.setFill(Color.RED);
        gc.fillOval(getX(), getY(), 0.0025, 0.0025);
        new Circle(getX(), getY(), 2, Color.RED);
    }
}