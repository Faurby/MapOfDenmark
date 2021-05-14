package bfst21.osm;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Objects;


/**
 * Pin is used to draw an image at a specific location.
 */
public enum Pin {

    ORIGIN("grey_pin32.png"),
    DESTINATION("destination_pin32.png"),
    USER_NODE("red_pin32.png");

    private boolean visible = false;
    private float[] coords;
    private final Image image;

    Pin(String imageStr) {
        this.image = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(imageStr)));
    }

    public void draw(GraphicsContext gc, double zoomLevel) {

        if (visible && coords != null) {
            double x = coords[0] - (10.0D / zoomLevel);
            double y = coords[1] - (30.0D / zoomLevel);
            gc.drawImage(image, x, y, 20.0D / zoomLevel, 30.0D / zoomLevel);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setCoords(float[] coords) {
        this.coords = coords;
    }

    public boolean isVisible() {
        return visible;
    }

    public float[] getCoords() {
        return coords;
    }
}
