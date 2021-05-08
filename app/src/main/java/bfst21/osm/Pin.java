package bfst21.osm;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Objects;


public enum Pin {

    ORIGIN("greyPin.png"),
    DESTINATION("redPin.png"),
    USER_NODE("bluePin.png");

    private boolean visible = false;
    private float[] coords;
    private final Image image;

    Pin(String imageStr) {
        this.image = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(imageStr)));
    }

    public void draw(GraphicsContext gc, double zoomLevel) {

        if (visible && coords != null) {
            double x = coords[0] - (10 / zoomLevel);
            double y = coords[1] - (30 / zoomLevel);

            gc.drawImage(image, x, y, 20 / zoomLevel, 30 / zoomLevel);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setCoords(float x, float y) {
        this.coords = new float[]{x, y};
    }

    public boolean isVisible() {
        return visible;
    }

    public float[] getCoords() {
        return coords;
    }
}
