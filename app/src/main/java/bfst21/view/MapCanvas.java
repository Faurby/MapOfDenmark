package bfst21.view;

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

    private final double zoomLevelMin = 50;
    private final double zoomLevelMax = 50000.0;
    private double zoomLevel;
    private double widthModifier = 1.0;

    private long averageRepaintTime;
    private long totalRepaintTime;
    private long totalRepaints;

    private final Options options = Options.getInstance();
    private final GraphicsContext gc = getGraphicsContext2D();

    private int depth;

    private ColorMode colorMode = ColorMode.STANDARD;
    private Affine trans = new Affine();

    public void init(Model model) {
        this.model = model;
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

                x1 -= 300;
                y1 -= 300;
                x2 += 300;
                y2 += 300;

                Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
                Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

                BoundingBox boundingBox = new BoundingBox((float) p1.getX(), (float) p2.getX(), (float) p1.getY(), (float) p2.getY());
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
                drawRoad(WayType.RESIDENTIAL);
                drawRoad(WayType.MOTORWAY);
                drawRoad(WayType.TERTIARY);
                drawRoad(WayType.PRIMARY);
            }
            if (options.getBool(Option.DISPLAY_WATER)) {
                drawRoad(WayType.WATERWAY);
            }
            if (options.getBool(Option.DISPLAY_BUILDINGS)) {
                paintFill(WayType.BUILDING);
                drawLine(WayType.BUILDING);
            }
        }
        gc.restore();

        time += System.nanoTime();
        System.out.println("Repaint time: "+time/1_000_000+" (Average: "+averageRepaintTime/1_000_000+" total repaints: "+totalRepaints+")");

        totalRepaintTime += time;
        totalRepaints++;

        averageRepaintTime = totalRepaintTime / totalRepaints;
    }

    public void drawKdTree(KdNode kdNode,
                           float maxX,
                           float maxY,
                           float minX,
                           float minY,
                           double lineWidth) {

        if (kdNode != null) {

            float kMinX = kdNode.getMinX();
            float kMaxX = kdNode.getMaxX();
            float kMinY = kdNode.getMinY();
            float kMaxY = kdNode.getMaxY();

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
                gc.moveTo(kMinX, maxY);
                gc.lineTo(kMinX, minY);
                gc.stroke();
                gc.moveTo(kMaxX, maxY);
                gc.lineTo(kMaxX, minY);
                gc.stroke();

                depth++;
                drawKdTree(kdNode.getLeftChild(), kMaxX, maxY, minX, minY, lineWidth);
                drawKdTree(kdNode.getRightChild(), maxX, maxY, kMinX, minY, lineWidth);

            } else {
                gc.moveTo(maxX, kMinY);
                gc.lineTo(minX, kMinY);
                gc.stroke();
                gc.moveTo(maxX, kMaxY);
                gc.lineTo(minX, kMaxY);
                gc.stroke();

                depth++;
                drawKdTree(kdNode.getLeftChild(), maxX, kMaxY, minX, minY, lineWidth);
                drawKdTree(kdNode.getRightChild(), maxX, maxY, minX, kMinY, lineWidth);
            }
            depth--;
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
        if (zoomLevel >= wayType.getZoomLevelRequired()) {
            gc.setStroke(getColor(wayType));
            gc.setLineWidth(1 / Math.sqrt(trans.determinant()));
            for (Way line : model.getMapData().getWays(wayType)) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public void paintFill(WayType wayType) {
        if (zoomLevel >= wayType.getZoomLevelRequired()) {
            gc.setFill(getColor(wayType));
            for (Way line : model.getMapData().getWays(wayType)) {
                line.fill(gc, zoomLevel);
            }
        }
    }

    public void drawRoad(WayType wayType) {
        if (zoomLevel >= wayType.getZoomLevelRequired()) {
            double size = wayType.getDrawSize() * widthModifier;

            gc.setStroke(getColor(wayType));
            gc.setLineWidth(size);
            for (Way line : model.getMapData().getWays(wayType)) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public Color getColor(WayType wayType) {
        if (colorMode == ColorMode.COLOR_BLIND) {
            return wayType.getColorBlind();
        } else if (colorMode == ColorMode.BLACK_WHITE) {
            return wayType.getBlackWhite();
        }
        return wayType.getColor();
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
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

    public String getZoomPercent() {
        if (zoomLevel == 0) {
            return 100 + "%";
        } else {
            double max = Math.log(zoomLevelMax);
            double min = Math.log(zoomLevelMin);
            double diff = max - min;
            double forOnePercent = diff / 100;

            double current = Math.log(zoomLevel) / forOnePercent;

            return (int) current + "%";
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
