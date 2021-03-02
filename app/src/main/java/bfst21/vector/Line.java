package bfst21.vector;

import java.io.Serializable;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class Line implements Drawable, Serializable {
    private static final long serialVersionUID = 1007313956054198315L;
    Point2D from, to;

    public Line(String line) {
        var parts = line.split(" ");
        var x1 = Double.parseDouble(parts[1]);
        var y1 = Double.parseDouble(parts[2]);
        var x2 = Double.parseDouble(parts[3]);
        var y2 = Double.parseDouble(parts[4]);
        from = new Point2D(x1, y1);
        to = new Point2D(x2, y2);
    }

    public Line(Point2D from, Point2D to) {
        this.from = from;
        this.to = to;
    }

    public void trace(GraphicsContext gc) {
        gc.moveTo(from.getX(), from.getY());
        gc.lineTo(to.getX(), to.getY());
    }

    public String toString() {
        return String.format("LINE %f %f %f %f", from.getX(), from.getY(), to.getX(), to.getY());
    }
}
