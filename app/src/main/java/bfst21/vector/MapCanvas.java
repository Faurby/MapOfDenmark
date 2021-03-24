package bfst21.vector;

import bfst21.models.Config;
import bfst21.vector.osm.Way;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import java.io.IOException;
import java.util.List;


public class MapCanvas extends Canvas {

    private Model model;
    private Affine trans = new Affine();
    private double zoomLevel = 1.0;
    private double zoomLevelMin = 0.018682;
    private double zoomLevelMax = 80.0;
    private double widthModifier = 1.0;
    private Config config;

    private ColorMode colorMode = ColorMode.STANDARD;

    public void init(Model model) throws IOException {
        this.model = model;
        this.config = new Config();

        pan(-model.getMapData().getMinx(), -model.getMapData().getMiny());
        zoom(getWidth() / (model.getMapData().getMaxx() - model.getMapData().getMinx()), new Point2D(0, 0));
    }

    void repaint() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(getColor("water"));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        paintFill(gc, model.getMapData().getIslands(), getColor("island"));
        paintFill(gc, model.getMapData().getLandUse(), getColor("landuse"));
        paintFill(gc, model.getMapData().getWater(), getColor("water"));

        drawRoadOutline(gc, model.getMapData().getExtendedWays("residential"), 0.0002, getColor("residential.outline"));
        drawRoadOutline(gc, model.getMapData().getExtendedWays("motorway"), 0.0004, getColor("motorway.outline"));
        drawRoadOutline(gc, model.getMapData().getExtendedWays("tertiary"), 0.0004, getColor("tertiary.outline"));

        drawRoad(gc, model.getMapData().getExtendedWays("residential"), 0.0002, getColor("residential"));
        drawRoad(gc, model.getMapData().getExtendedWays("motorway"), 0.0004, getColor("motorway"));
        drawRoad(gc, model.getMapData().getExtendedWays("tertiary"), 0.0004, getColor("tertiary"));

        if (zoomLevel > 4) {
            drawRoad(gc, model.getMapData().getWaterWays(), 0.0002, getColor("water"));
            paintFill(gc, model.getMapData().getBuildings(), getColor("building"));
            drawLine(gc, model.getMapData().getBuildings(), getColor("building"));
        }
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

    public void drawRoad(GraphicsContext gc, List<Way> list, double size, Color roadColor) {
        adjustWidthModifier();
        size *= widthModifier;
        gc.setStroke(roadColor);
        gc.setLineWidth(size * 0.75);
        for (Way line : list) {
            line.draw(gc);
        }
    }

    public void drawRoadOutline(GraphicsContext gc, List<Way> list, double size, Color outline) {
        adjustWidthModifier();
        size *= widthModifier;
        gc.setStroke(outline);
        gc.setLineWidth(size);
        for (Way line : list) {
            line.draw(gc);
        }
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public Color getColor(String type) {
        System.out.println(colorMode.toString().toLowerCase());
        String prop = config.getProp("colors."+type+"."+colorMode.toString().toLowerCase());
        System.out.println("Prop: "+prop);
        Color color = Color.valueOf("#"+prop);

        if (color != null) {
            return color;
        }
        return Color.RED;
    }

    public void adjustWidthModifier() {
        if (zoomLevel < 5) {
            widthModifier = 1;
        } else if (zoomLevel < 15) {
            widthModifier = 0.75;
        } else if (zoomLevel < 35) {
            widthModifier = 0.50;
        } else if (zoomLevel < 80) {
            widthModifier = 0.25;
        }
    }
}
