package bfst21.vector;

import bfst21.vector.osm.Way;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import java.util.List;


public class MapCanvas extends Canvas {

    private Model model;
    private Affine trans = new Affine();
    private double zoomLevel = 1.0;
    private double zoomLevelMin = 0.018682;
    private double zoomLevelMax = 80.0;

    public void init(Model model) {
        this.model = model;
        pan(-model.getMapData().getMinx(), -model.getMapData().getMiny());
        zoom(getWidth() / (model.getMapData().getMaxx() - model.getMapData().getMinx()), new Point2D(0, 0));
    }

    void repaint() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);

        paintFill(gc, model.getMapData().getIslands(), Color.LIGHTYELLOW);
        paintFill(gc, model.getMapData().getBuildings(), Color.LIGHTGRAY);
        drawLine(gc, model.getMapData().getBuildings(), Color.DARKGRAY);
        drawRoad(gc, model.getMapData().getExtendedWays(), 0.00001, Color.DARKGREY, Color.BLACK);
        gc.restore();
    }

    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }

    public void preZoom(double factor, Point2D center) {
        double zoomLevelNext = zoomLevel * factor;
        if (zoomLevelNext < zoomLevelMax && zoomLevelNext > zoomLevelMin) {
            zoomLevel = zoomLevelNext;
            zoom(factor, center);
        }
    }

    public void zoom(double factor, Point2D center) {
        trans.prependScale(factor, factor, center);
        repaint();
        System.out.println(zoomLevel);
    }

    public Point2D mouseToModelCoords(Point2D point) {
        try {
            return trans.inverseTransform(point);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void drawLine(GraphicsContext gc, List<Way> list, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(1 / Math.sqrt(trans.determinant()));
        for (Way line : list) {
            line.draw(gc);
        }
    }

    public void paintFill(GraphicsContext gc, List<Way> list, Color color) {
        gc.setFill(color);
        for (Way line : list) {
            line.fill(gc);
        }
    }

    public void drawRoad(GraphicsContext gc, List<Way> list, double size, Color roadColor, Color outline) {
        gc.setStroke(outline);
        gc.setLineWidth(size);
        for (Way line : list) {
            line.draw(gc);
        }
        gc.setStroke(roadColor);
        gc.setLineWidth(size * 0.75);
        for (Way line : list) {
            line.draw(gc);
        }
    }
}