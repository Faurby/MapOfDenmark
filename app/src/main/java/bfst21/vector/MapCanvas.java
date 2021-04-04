package bfst21.vector;

import bfst21.models.Config;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdNode;
import bfst21.tree.KdTree;
import bfst21.vector.osm.Way;
import bfst21.vector.osm.WayType;
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

    private boolean debug = false;

    private int depth = 0;

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

    //TODO: FIX max/min x/y you know
    public void draw(KdNode kdNode,
                     float maxX,
                     float maxY,
                     float minX,
                     float minY,
                     double lineWidth) {

        if (kdNode != null) {

            float x = kdNode.getX();
            float y = kdNode.getY();

            gc.setStroke(Color.PURPLE);
            if (depth == 0) {
                gc.setStroke(Color.RED);
            } else if (depth == 1) {
                gc.setStroke(Color.TURQUOISE);
            } else if (depth == 2) {
                gc.setStroke(Color.TEAL);
            }

            gc.setLineWidth(lineWidth);
            gc.beginPath();

            lineWidth *= 0.8D;

            if (depth % 2 == 0) {
                gc.moveTo(x, maxY);
                gc.lineTo(x, minY);
                gc.stroke();

                depth++;
                draw(kdNode.getLeftChild(), x, maxY, minX, minY, lineWidth);
                draw(kdNode.getRightChild(), maxX, maxY, x, minY, lineWidth);

            } else {
                gc.moveTo(maxX, y);
                gc.lineTo(minX, y);
                gc.stroke();

                depth++;
                draw(kdNode.getLeftChild(), maxX, y, minX, minY, lineWidth);
                draw(kdNode.getRightChild(), maxX, maxY, minX, y, lineWidth);
            }
            depth--;

            gc.setStroke(Color.GREENYELLOW);
            gc.setLineWidth(lineWidth * 3);
            gc.beginPath();
            gc.moveTo(kdNode.getX(), kdNode.getY());
            gc.lineTo(kdNode.getX() + 0.00001, kdNode.getY() + 0.00001);
            gc.stroke();
        }
    }

    void repaint() {
        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(getColor(WayType.WATER));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        double x1 = trans.getTx() / Math.sqrt(trans.determinant());
        double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
        double x2 = this.getWidth() - x1;
        double y2 = this.getHeight() - y1;

        x1 -= 100;
        y1 -= 200;
        x2 += 100;
        y2 += 200;

        Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
        Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

        BoundingBox boundingBox = new BoundingBox((float) p2.getX(), (float) p1.getX(), (float) p2.getY(), (float) p1.getY());
        model.getMapData().rangeSearch(boundingBox);

        paintFill(model.getMapData().getIslands(), getColor(WayType.ISLAND), getDrawAtZoom(WayType.ISLAND));

        if (debug) {
            depth = 0;

            float maxX = model.getMapData().getMaxx();
            float maxY = model.getMapData().getMaxy();
            float minX = model.getMapData().getMinx();
            float minY = model.getMapData().getMiny();

//            float maxX = boundingBox.getMaxX();
//            float maxY = boundingBox.getMaxY();
//            float minX = boundingBox.getMinX();
//            float minY = boundingBox.getMinY();

            KdTree kdTree = model.getMapData().getKdTree();
            draw(kdTree.getRoot(), maxX, maxY, minX, minY, 0.001);

            gc.setStroke(Color.RED);
            gc.setLineWidth(0.001);
            gc.beginPath();
            gc.moveTo(p1.getX(), p1.getY());
            gc.lineTo(p2.getX(), p1.getY());
            gc.lineTo(p2.getX(), p2.getY());
            gc.lineTo(p1.getX(), p2.getY());
            gc.lineTo(p1.getX(), p1.getY());
            gc.stroke();
        }

        paintFill(model.getMapData().getLandUse(), getColor(WayType.LANDUSE), getDrawAtZoom(WayType.LANDUSE));
        paintFill(model.getMapData().getWater(), getColor(WayType.WATER), getDrawAtZoom(WayType.WATER));

        drawRoadOutline(model.getMapData().getExtendedWays(WayType.RESIDENTIAL), 0.0002, getColor(WayType.RESIDENTIAL, true), getDrawAtZoom(WayType.RESIDENTIAL));
        drawRoadOutline(model.getMapData().getExtendedWays(WayType.MOTORWAY), 0.0004, getColor(WayType.MOTORWAY, true), getDrawAtZoom(WayType.MOTORWAY));
        drawRoadOutline(model.getMapData().getExtendedWays(WayType.TERTIARY), 0.0004, getColor(WayType.TERTIARY, true), getDrawAtZoom(WayType.TERTIARY));

        drawRoad(model.getMapData().getExtendedWays(WayType.RESIDENTIAL), 0.0002, getColor(WayType.RESIDENTIAL), getDrawAtZoom(WayType.RESIDENTIAL));
        drawRoad(model.getMapData().getExtendedWays(WayType.MOTORWAY), 0.0004, getColor(WayType.MOTORWAY), getDrawAtZoom(WayType.MOTORWAY));
        drawRoad(model.getMapData().getExtendedWays(WayType.TERTIARY), 0.0004, getColor(WayType.TERTIARY), getDrawAtZoom(WayType.TERTIARY));

        drawRoad(model.getMapData().getWaterWays(), 0.0002, getColor(WayType.WATERWAY), getDrawAtZoom(WayType.WATERWAY));
        paintFill(model.getMapData().getBuildings(), getColor(WayType.BUILDING), getDrawAtZoom(WayType.BUILDING));
        drawLine(model.getMapData().getBuildings(), getColor(WayType.BUILDING), getDrawAtZoom(WayType.BUILDING));

        gc.restore();
    }

    public void drawBoundingBox(BoundingBox boundingBox, Color color, double size) {
        float bMaxX = boundingBox.getMaxX();
        float bMaxY = boundingBox.getMaxY();
        float bMinX = boundingBox.getMinX();
        float bMinY = boundingBox.getMinY();

        gc.setStroke(color);
        gc.setLineWidth(size);
        gc.beginPath();
        gc.moveTo(bMinX, bMinY);
        gc.lineTo(bMaxX, bMinY);
        gc.lineTo(bMaxX, bMaxY);
        gc.lineTo(bMinX, bMaxY);
        gc.lineTo(bMinX, bMinY);
        gc.stroke();
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

    public Color getColor(WayType type, boolean outline) {
        String path = "colors." + type.toString().toLowerCase() + "." + colorMode.toString().toLowerCase();
        if (outline) {
            path = "colors." + type.toString().toLowerCase() + ".outline." + colorMode.toString().toLowerCase();
        }
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

    public Color getColor(WayType type) {
        return getColor(type, false);
    }

    public int getDrawAtZoom(WayType type) {
        String path = "zoom." + type.toString().toLowerCase();
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

    public void toggleDebug() {
        debug = !debug;
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
