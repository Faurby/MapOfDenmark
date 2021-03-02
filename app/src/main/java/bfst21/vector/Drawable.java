package bfst21.vector;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
    public default void draw(GraphicsContext gc) {
        gc.beginPath();
        trace(gc);
        gc.stroke();
    }
    public default void fill(GraphicsContext gc) {
        gc.beginPath();
        trace(gc);
        gc.fill();
    }
    public void trace(GraphicsContext gc);
}
