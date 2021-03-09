package bfst21.vector;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
    default void draw(GraphicsContext gc) {
        gc.beginPath();
        trace(gc);
        gc.stroke();
    }

    default void fill(GraphicsContext gc) {
        gc.beginPath();
        trace(gc);
        gc.fill();
    }

    void trace(GraphicsContext gc);
}
