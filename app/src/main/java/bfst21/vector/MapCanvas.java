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

    private double zoomLevel = 1.0;
    private double zoomLevelMin = 0.018682;
    private double zoomLevelMax = 80.0;
    private double widthModifier = 1.0;

    private Config config;
    private ColorMode colorMode = ColorMode.STANDARD;
    private GraphicsContext gc = getGraphicsContext2D();
    private Affine trans = new Affine();

    public void init(Model model) throws IOException {
        this.model = model;
        this.config = new Config();

        pan(-model.getMapData().getMinx(), -model.getMapData().getMiny());
        zoom(getWidth() / (model.getMapData().getMaxx() - model.getMapData().getMinx()), new Point2D(0, 0));
    }

    void repaint() {
        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(getColor("water"));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        paintFill(model.getMapData().getIslands(), getColor("island"));
        paintFill(model.getMapData().getLandUse(), getColor("landuse"));
        paintFill(model.getMapData().getWater(), getColor("water"));

        drawRoadOutline(model.getMapData().getExtendedWays("residential"), 0.0002, getColor("residential.outline"));
        drawRoadOutline(model.getMapData().getExtendedWays("motorway"), 0.0004, getColor("motorway.outline"));
        drawRoadOutline(model.getMapData().getExtendedWays("tertiary"), 0.0004, getColor("tertiary.outline"));

        drawRoad(model.getMapData().getExtendedWays("residential"), 0.0002, getColor("residential"));
        drawRoad(model.getMapData().getExtendedWays("motorway"), 0.0004, getColor("motorway"));
        drawRoad(model.getMapData().getExtendedWays("tertiary"), 0.0004, getColor("tertiary"));

        if (zoomLevel > 4) {
            drawRoad(model.getMapData().getWaterWays(), 0.0002, getColor("water"));
            paintFill(model.getMapData().getBuildings(), getColor("building"));
            drawLine(model.getMapData().getBuildings(), getColor("building"));
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
        } else if (zoomLevelNext > zoomLevelMax && zoomLevel != zoomLevelMax){
            zoomLevel = zoomLevelMax;
            zoom(factor, center);
        }
    }

    public void zoom(double factor, Point2D center) {
        trans.prependScale(factor, factor, center);
        repaint();
    }

    public Point2D mouseToModelCoords(Point2D point) {
        try {
            return trans.inverseTransform(point);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void drawLine(List<Way> list, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(1 / Math.sqrt(trans.determinant()));
        for (Way line : list) {
            line.draw(gc);
        }
    }

    public void paintFill(List<Way> list, Color color) {
        gc.setFill(color);
        for (Way line : list) {
            line.fill(gc);
        }
    }

    public void drawRoad(List<Way> list, double size, Color roadColor) {
        adjustWidthModifier();
        size *= widthModifier;
        gc.setStroke(roadColor);
        gc.setLineWidth(size * 0.75);
        for (Way line : list) {
            line.draw(gc);
        }
    }

    public void drawRoadOutline(List<Way> list, double size, Color outline) {
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
        String path = "colors."+type+"."+colorMode.toString().toLowerCase();
        try {
            Color color = Color.valueOf("#" + config.getProp(path));
            if (color != null) {
                return color;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid color code at " + path);
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

    //TODO: Fix better representation of zoom. Problem is the difference
    public String getZoomPercent() {
        if (zoomLevel == 1 ){
            return 100 + "%";
        } else if (zoomLevel > 1) {
            return Math.round((zoomLevel/zoomLevelMax)*100*100)/100 + 100 + "%";
        } else {
            return Math.round(zoomLevel*100*100)/100 + "%";
        }

    }
}
