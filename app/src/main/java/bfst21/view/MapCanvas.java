package bfst21.view;

import bfst21.models.Option;
import bfst21.models.Options;
import bfst21.osm.Relation;
import bfst21.osm.UserNode;
import bfst21.pathfinding.Edge;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdNode;
import bfst21.osm.Way;
import bfst21.osm.ElementType;
import bfst21.models.Model;
import bfst21.tree.KdTree;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;


public class MapCanvas extends Canvas {

    private Model model;

    private final double zoomLevelMin = 50;
    private final double zoomLevelMax = 100_000.0;
    private double zoomLevel;
    private double widthModifier = 1.0;

    private long totalRepaints;
    private long totalLastRepaintTime;
    private long lastTenAverageRepaintTime;

    private final Options options = Options.getInstance();
    private final GraphicsContext gc = getGraphicsContext2D();

    private int depth;
    private Task<Void> rangeSearchTask;

    private ColorMode colorMode = ColorMode.STANDARD;
    private Affine trans = new Affine();

    private boolean initialRangeSearch;

    public void init(Model model) {
        this.model = model;
    }

    public void load(boolean loadDefault) throws XMLStreamException, IOException, ClassNotFoundException {
        initialRangeSearch = false;
        model.load(loadDefault);
        trans = new Affine();

        pan(-model.getMapData().getMinx(), -model.getMapData().getMiny());
        double factor = getWidth() / (model.getMapData().getMaxx() - model.getMapData().getMinx());

        zoomLevel = 1.0D;
        zoomLevel *= factor;
        System.out.println("Zoom: " + zoomLevel + " factor: " + factor);

        initialZoom(factor, new Point2D(0, 0));
    }

    void repaint() {
        long time = -System.nanoTime();

        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(getColor(ElementType.WATER));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setTransform(trans);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        if (model.getMapData() != null) {

            if (options.getBool(Option.DISPLAY_ISLANDS)) {
                paintFill(ElementType.ISLAND);
            }

            if (!initialRangeSearch) {
                if (options.getBool(Option.USE_KD_TREE)) {
                    doRangeSearch();

                } else if (options.getBool(Option.USE_R_TREE)) {
                    doRangeSearch();
                }
            }
            adjustWidthModifier();

            if (options.getBool(Option.DISPLAY_WATER)) {
                paintFill(ElementType.WATER);
                drawRoad(ElementType.WATERWAY);
            }

            if (options.getBool(Option.DISPLAY_LAND_USE)) {
                paintFill(ElementType.LANDUSE);
            }

            gc.setLineDashes(0.0001);
            drawRoad(ElementType.CYCLEWAY);
            drawRoad(ElementType.FOOTWAY);
            gc.setLineDashes(0);
            drawRoad(ElementType.ROAD);

            if (options.getBool(Option.DISPLAY_WAYS)) {
                drawRoad(ElementType.RESIDENTIAL);
                drawRoad(ElementType.MOTORWAY);
                drawRoad(ElementType.TERTIARY);
                drawRoad(ElementType.PRIMARY);
            }
            if (options.getBool(Option.DISPLAY_BUILDINGS)) {
                paintFill(ElementType.BUILDING);
            }
            if (options.getBool(Option.USE_KD_TREE)) {
                if (options.getBool(Option.DISPLAY_KD_TREE)) {
                    depth = 0;

                    float maxX = model.getMapData().getMaxx();
                    float maxY = model.getMapData().getMaxy();
                    float minX = model.getMapData().getMinx();
                    float minY = model.getMapData().getMiny();

                    for (ElementType elementType : ElementType.values()) {
                        if (zoomLevel >= elementType.getZoomLevelRequired()) {
                            KdTree kdTree = model.getMapData().getKdTree(elementType);
                            drawKdTree(kdTree.getRoot(), maxX, maxY, minX, minY, 0.001);
                        }
                    }
                }
            }

            drawUserNodes();
            drawRelations();
            if (options.getBool(Option.DISPLAY_GRAPH)) {
                drawGraph();
            }
            drawRoad(ElementType.UNKNOWN);

        }
        gc.restore();

        time += System.nanoTime();

        totalLastRepaintTime += time;
        totalRepaints++;

        if (totalRepaints % 10 == 0) {
            lastTenAverageRepaintTime = totalLastRepaintTime / 10_000_000;
            System.out.println("Repaint time: " + time / 1_000_000 + " (Average last ten repaints: " + lastTenAverageRepaintTime + " total repaints: " + totalRepaints + ")");

            totalLastRepaintTime = 0;
        }
    }

    public void doRangeSearch() {
        initialRangeSearch = true;

        if (model.getMapData() != null) {
            if (options.getBool(Option.USE_KD_TREE)) {
                double x1 = trans.getTx() / Math.sqrt(trans.determinant());
                double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
                double x2 = getWidth() - x1;
                double y2 = getHeight() - y1;

                x1 -= 50;
                y1 -= 50;
                x2 += 50;
                y2 += 50;

                Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
                Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

                BoundingBox boundingBox = new BoundingBox((float) p1.getX(), (float) p2.getX(), (float) p1.getY(), (float) p2.getY());
                model.getMapData().kdTreeRangeSearch(boundingBox, zoomLevel);

            } else if (options.getBool(Option.USE_R_TREE)) {
                double x1 = trans.getTx() / Math.sqrt(trans.determinant());
                double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
                double x2 = getWidth() - x1;
                double y2 = getHeight() - y1;

                Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
                Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

                model.getMapData().rTreeRangeSearch(p1.getX(), p1.getY(), p2.getX(), p2.getY(), zoomLevel);
            }
        }
    }

    private void drawGraph() {
        gc.setStroke(Color.DARKSLATEBLUE);
        gc.setLineWidth(0.0002 * widthModifier);

        for (Edge edge : model.getMapData().getDirectedGraph().getEdges()) {
            edge.draw(gc, zoomLevel);
        }
    }

    private void drawUserNodes() {
        gc.setStroke(Color.RED);
        gc.setLineWidth(0.002 * widthModifier);

        for (UserNode userNode : model.getMapData().getUserNodes()) {
            userNode.draw(gc, 0);
        }
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

    public void initialZoom(double factor, Point2D center) {
        trans.prependScale(factor, factor, center);
        repaint();
    }

    public void zoom(double factor, Point2D center) {
        trans.prependScale(factor, factor, center);
        repaint();
        runRangeSearchTask();
    }

    public void runRangeSearchTask() {
        if (rangeSearchTask != null) {
            if (rangeSearchTask.isRunning()) {
                rangeSearchTask.cancel();
            }
        }
        rangeSearchTask = new Task<>() {
            @Override
            protected Void call() {
                doRangeSearch();
                return null;
            }
        };
        rangeSearchTask.setOnSucceeded(e -> repaint());
        rangeSearchTask.setOnFailed(e -> rangeSearchTask.getException().printStackTrace());
        Thread thread = new Thread(rangeSearchTask);
        thread.start();
    }

    public Point2D mouseToModelCoords(Point2D point) {
        try {
            return trans.inverseTransform(point);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void drawRelations() {
//        for (Relation rel : model.getMapData().getRelations()) {
//            for (Way way : rel.getWays()) {
//                if (way.getType() != null) {
//                    ElementType elementType = way.getType();
//                    if (zoomLevel >= elementType.getZoomLevelRequired()) {
//                        if (elementType == ElementType.BUILDING ||
//                            elementType == ElementType.LANDUSE ||
//                            elementType == ElementType.WATER) {
//                            gc.setFill(elementType.getColor());
//                            way.fill(gc, zoomLevel);
//                        } else {
//                            double size = elementType.getDrawSize() * widthModifier;
//                            gc.setStroke(getColor(elementType));
//                            gc.setLineWidth(size);
//                            way.draw(gc, zoomLevel);
//                        }
//                    }
//                }
//            }
//        }
        gc.setFillRule(FillRule.EVEN_ODD);
        for (Relation rel : model.getMapData().getRelations()) {
            if (rel.getType() != null) {
                ElementType elementType = rel.getType();
                if (zoomLevel >= elementType.getZoomLevelRequired()) {
                    gc.setFill(elementType.getColor());
                    rel.fill(gc, zoomLevel);
                }
            }
        }
    }

    public void paintFill(ElementType elementType) {
        if (zoomLevel >= elementType.getZoomLevelRequired()) {
            gc.setFill(getColor(elementType));
            //for (Way way : model.getMapData().getFillWays(elementType, zoomLevel)) {
            for (Way way : model.getMapData().getWays(elementType)) {
                way.fill(gc, zoomLevel);
            }
        }
    }

    public void drawRoad(ElementType elementType) {
        if (zoomLevel >= elementType.getZoomLevelRequired()) {
            double size = elementType.getDrawSize() * widthModifier;

            gc.setStroke(getColor(elementType));
            gc.setLineWidth(size);
            for (Way line : model.getMapData().getWays(elementType)) {
                line.draw(gc, zoomLevel);
            }
        }
    }

    public Color getColor(ElementType elementType) {
        if (colorMode == ColorMode.COLOR_BLIND) {
            return elementType.getColorBlind();
        } else if (colorMode == ColorMode.BLACK_WHITE) {
            return elementType.getBlackWhite();
        }
        return elementType.getColor();
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public void adjustWidthModifier() {
        if (zoomLevel < 500) {
            widthModifier = 1.0D;

        } else if (zoomLevel < 2000) {
            widthModifier = 0.75;

        } else if (zoomLevel < 6000) {
            widthModifier = 0.50;

        } else if (zoomLevel < 22000) {
            widthModifier = 0.25;
        }
    }

    public String getAverageRepaintTime() {
        return lastTenAverageRepaintTime + "ms";
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
