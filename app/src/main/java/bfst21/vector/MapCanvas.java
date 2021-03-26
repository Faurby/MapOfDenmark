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

        paintFill(model.getMapData().getIslands(), getColor("island"), getDrawAtZoom("island"));
        paintFill(model.getMapData().getLandUse(), getColor("landuse"), getDrawAtZoom("landuse"));
        paintFill(model.getMapData().getWater(), getColor("water"), getDrawAtZoom("water"));

        drawRoadOutline(model.getMapData().getExtendedWays("residential"), 0.0002, getColor("residential.outline"), getDrawAtZoom("residential"));
        drawRoadOutline(model.getMapData().getExtendedWays("motorway"), 0.0004, getColor("motorway.outline"), getDrawAtZoom("motorway"));
        drawRoadOutline(model.getMapData().getExtendedWays("tertiary"), 0.0004, getColor("tertiary.outline"), getDrawAtZoom("tertiary"));

        drawRoad(model.getMapData().getExtendedWays("residential"), 0.0002, getColor("residential"), getDrawAtZoom("residential"));
        drawRoad(model.getMapData().getExtendedWays("motorway"), 0.0004, getColor("motorway"), getDrawAtZoom("motorway"));
        drawRoad(model.getMapData().getExtendedWays("tertiary"), 0.0004, getColor("tertiary"), getDrawAtZoom("tertiary"));

        drawRoad(model.getMapData().getWaterWays(), 0.0002, getColor("water"), getDrawAtZoom("waterWay"));
        paintFill(model.getMapData().getBuildings(), getColor("building"), getDrawAtZoom("building"));
        drawLine(model.getMapData().getBuildings(), getColor("building"), getDrawAtZoom("building"));

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
            System.out.println("Zoom: " + zoomLevel);
        } else if (zoomLevelNext > zoomLevelMax && zoomLevel != zoomLevelMax) {
            zoomLevel = zoomLevelMax;
            zoom(factor, center);
            System.out.println("Zoom: " + zoomLevel);
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

    public void drawLine(List<Way> list, Color color, int drawAtZoom) {
        int currentZoomPercent = Integer.parseInt(getZoomPercent().substring(0, getZoomPercent().length() - 1));
        if (drawAtZoom <= currentZoomPercent) {
            gc.setStroke(color);
            gc.setLineWidth(1 / Math.sqrt(trans.determinant()));
            for (Way line : list) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public void paintFill(List<Way> list, Color color, int drawAtZoom) {
        int currentZoomPercent = Integer.parseInt(getZoomPercent().substring(0, getZoomPercent().length() - 1));
        if (drawAtZoom <= currentZoomPercent) {
            gc.setFill(color);
            for (Way line : list) {
                line.fill(gc, zoomLevel);
            }
        }
    }

    public void drawRoad(List<Way> list, double size, Color roadColor, int drawAtZoom) {
        int currentZoomPercent = Integer.parseInt(getZoomPercent().substring(0, getZoomPercent().length() - 1));
        if (drawAtZoom <= currentZoomPercent) {
            adjustWidthModifier();
            size *= widthModifier;
            gc.setStroke(roadColor);
            gc.setLineWidth(size * 0.75);
            for (Way line : list) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public void drawRoadOutline(List<Way> list, double size, Color outline, int drawAtZoom) {
        int currentZoomPercent = Integer.parseInt(getZoomPercent().substring(0, getZoomPercent().length() - 1));
        if (drawAtZoom <= currentZoomPercent) {
            adjustWidthModifier();
            size *= widthModifier;
            gc.setStroke(outline);
            gc.setLineWidth(size);
            for (Way line : list) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public Color getColor(String type) {
        String path = "colors." + type + "." + colorMode.toString().toLowerCase();
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

    public int getDrawAtZoom(String type) {
        //String zoomPercent = getZoomPercent();
        //String currentZoomPercent = zoomPercent.substring(0, zoomPercent.length() - 1);
        String path = "zoom." + type;
        try {
            int drawAtZoom = Integer.parseInt(config.getProp(path));
            if (drawAtZoom > -1) {
                return drawAtZoom;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid draw zoom level at " + path);
        }
        return 0;
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
        if (zoomLevel == 1) {
            return 100 + "%";
        } else if (zoomLevel > 1) {
            return Math.round((zoomLevel / zoomLevelMax) * 100 * 100) / 100 + 100 + "%";
        } else {
            return Math.round(zoomLevel * 100 * 100) / 100 + "%";
        }
    }
}
