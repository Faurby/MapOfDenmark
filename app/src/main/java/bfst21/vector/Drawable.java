package bfst21.vector;

import javafx.scene.canvas.GraphicsContext;


public interface Drawable {

    default void draw(GraphicsContext gc, double zoomLevel) {
        gc.beginPath();
        trace(gc, zoomLevel);
        gc.stroke();
    }

    default void fill(GraphicsContext gc, double zoomLevel) {
        gc.beginPath();
        trace(gc, zoomLevel);
        gc.fill();
    }

    void trace(GraphicsContext gc, double zoomLevel);
}
