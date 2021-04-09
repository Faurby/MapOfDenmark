package bfst21.view;

import bfst21.models.Config;
import bfst21.models.Option;
import bfst21.models.Options;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdNode;
import bfst21.tree.KdTree;
import bfst21.osm.Way;
import bfst21.osm.WayType;
import bfst21.models.Model;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;


public class MapCanvas extends Canvas {

    private Model model;

    private double zoomLevel;
    private double zoomLevelMin = 50;
    private double zoomLevelMax = 50000.0;
    private double widthModifier = 1.0;

    private final Options options = Options.getInstance();
    private final GraphicsContext gc = getGraphicsContext2D();

    private int depth = 0;

    private Config config;
    private ColorMode colorMode = ColorMode.STANDARD;
    private Affine trans = new Affine();

    public void init(Model model) throws IOException {
        this.model = model;
        this.config = new Config();
    }

    public void load(boolean loadDefault) throws XMLStreamException, IOException, ClassNotFoundException {
        model.load(loadDefault);
        trans = new Affine();

        pan(-model.getMapData().getMinx(), -model.getMapData().getMiny());
        double factor = getWidth() / (model.getMapData().getMaxx() - model.getMapData().getMinx());

        zoomLevel = 1.0D;
        zoomLevel *= factor;
        System.out.println("Zoom: " + zoomLevel + " factor: " + factor);

        zoom(factor, new Point2D(0, 0));
    }

    void repaint() {
        long time = -System.nanoTime();

        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(getColor(WayType.WATER));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        if (model.getMapData() != null) {

            if (options.getBool(Option.DISPLAY_ISLANDS)) {
                paintFill(WayType.ISLAND);
            }

            if (options.getBool(Option.USE_KD_TREE)) {
                double x1 = trans.getTx() / Math.sqrt(trans.determinant());
                double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
                double x2 = getWidth() - x1;
                double y2 = getHeight() - y1;

                Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
                Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

                BoundingBox boundingBox = new BoundingBox((float) p2.getX(), (float) p2.getY(), (float) p1.getX(), (float) p1.getY());
                model.getMapData().rangeSearch(boundingBox);

                if (options.getBool(Option.DISPLAY_KD_TREE)) {
                    depth = 0;

                    float maxX = model.getMapData().getMaxx();
                    float maxY = model.getMapData().getMaxy();
                    float minX = model.getMapData().getMinx();
                    float minY = model.getMapData().getMiny();

                    KdTree kdTree = model.getMapData().getKdTree();
                    drawKdTree(kdTree.getRoot(), maxX, maxY, minX, minY, 0.001);

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
            } else if (options.getBool(Option.USE_R_TREE)) {
                double x1 = trans.getTx() / Math.sqrt(trans.determinant());
                double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
                double x2 = getWidth() - x1;
                double y2 = getHeight() - y1;

                Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
                Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

                model.getMapData().search(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }

            adjustWidthModifier();

            if (options.getBool(Option.DISPLAY_LAND_USE)) {
                paintFill(WayType.LANDUSE);
            }
            if (options.getBool(Option.DISPLAY_WATER)) {
                paintFill(WayType.WATER);
            }

            if (options.getBool(Option.DISPLAY_WAYS)) {
                drawRoad(WayType.RESIDENTIAL, true);
                drawRoad(WayType.MOTORWAY, true);
                drawRoad(WayType.TERTIARY, true);
                drawRoad(WayType.PRIMARY, true);

                drawRoad(WayType.RESIDENTIAL, false);
                drawRoad(WayType.MOTORWAY, false);
                drawRoad(WayType.TERTIARY, false);
                drawRoad(WayType.PRIMARY, false);

            }
            if (options.getBool(Option.DISPLAY_WATER)) {
                drawRoad(WayType.WATERWAY, false);
            }
            if (options.getBool(Option.DISPLAY_BUILDINGS)) {
                paintFill(WayType.BUILDING);
                drawLine(WayType.BUILDING);
            }
        }
        gc.restore();

        time += System.nanoTime();
        System.out.println("Repaint time: " + time / 1_000_000);
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

    public void drawKdTree(KdNode kdNode,
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
                drawKdTree(kdNode.getLeftChild(), x, maxY, minX, minY, lineWidth);
                drawKdTree(kdNode.getRightChild(), maxX, maxY, x, minY, lineWidth);

            } else {
                gc.moveTo(maxX, y);
                gc.lineTo(minX, y);
                gc.stroke();

                depth++;
                drawKdTree(kdNode.getLeftChild(), maxX, y, minX, minY, lineWidth);
                drawKdTree(kdNode.getRightChild(), maxX, maxY, minX, y, lineWidth);
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
    }

    public Point2D mouseToModelCoords(Point2D point) {
        try {
            return trans.inverseTransform(point);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void drawLine(WayType wayType) {
        if (zoomLevel >= getDrawAtZoom(wayType)) {
            gc.setStroke(getColor(wayType));
            gc.setLineWidth(1 / Math.sqrt(trans.determinant()));
            for (Way line : model.getMapData().getWays(wayType)) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public void paintFill(WayType wayType) {
        if (zoomLevel >= getDrawAtZoom(wayType)) {
            gc.setFill(getColor(wayType));
            for (Way line : model.getMapData().getWays(wayType)) {
                line.fill(gc, zoomLevel);
            }
        }
    }

    public void drawRoad(WayType wayType, boolean outline) {
        if (zoomLevel >= getDrawAtZoom(wayType)) {
            double size = getSize(wayType) * widthModifier;

            gc.setStroke(getColor(wayType, outline));
            if (!outline) {
                size *= 0.75;
            }
            gc.setLineWidth(size);
            for (Way line : model.getMapData().getWays(wayType)) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public Color getColor(WayType type) {
        return getColor(type, false);
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

    public double getDrawAtZoom(WayType type) {
        String path = "zoom." + type.toString().toLowerCase();
        try {
            return Double.parseDouble(config.getProp(path));
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid draw zoom level at " + path);
        }
        return 0;
    }

    public double getSize(WayType type) {
        String path = "size." + type.toString().toLowerCase();
        try {
            return Double.parseDouble(config.getProp(path));
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid size value at " + path);
        }
        return 0;
    }

    public void adjustWidthModifier() {
        if (zoomLevel < 15000) {
            widthModifier = 1;

        } else if (zoomLevel < 45000) {
            widthModifier = 0.75;

        } else if (zoomLevel < 35 * 105000) {
            widthModifier = 0.50;

        } else if (zoomLevel < 240000) {
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

    public String getZoomLevel() {
        String value = "" + zoomLevel;
        if (value.length() > 8) {
            return value.substring(0, 8);
        }
        return value;
    }
}
