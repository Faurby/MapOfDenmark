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

    private final double zoomLevelMin = 50.0D, zoomLevelMax = 100_000.0D;
    private double zoomLevel;
    private double widthModifier = 1.0D;

    private long totalRepaints, totalLastRepaintTime, lastTenAverageRepaintTime;

    private final Options options = Options.getInstance();
    private final GraphicsContext gc = getGraphicsContext2D();

    private int depth;
    private Task<Void> rangeSearchTask;

    private ColorMode colorMode = ColorMode.STANDARD;
    private Affine trans = new Affine();

    /**
     * Initializes MapCanvas with the given Model.
     */
    public void init(Model model) {
        this.model = model;
    }

    /**
     * Loads selected file in Model.
     * Pans the MapCanvas based on the bounds for the loaded map.
     * Fits the MapCanvas to the screen.
     * @param loadDefaultFile determines if the default or selected file should be loaded.
     */
    public void load(boolean loadDefaultFile) throws XMLStreamException, IOException, ClassNotFoundException {
        model.load(loadDefaultFile);
        trans = new Affine();

        pan(-model.getMapData().getMinX(), -model.getMapData().getMinY());

        zoomLevel = 1.0D;
        double zoomFactor = getWidth() / (model.getMapData().getMaxX() - model.getMapData().getMinX());

        zoom(zoomFactor, new Point2D(0, 0));
    }

    /**
     * Repaints the MapCanvas.
     * Draws Ways for every ElementType to display at the current zoom level.
     * Draws every multipolygon relation.
     * Draws point of interests added by the user.
     */
    public void repaint() {
        long time = -System.nanoTime();

        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(getColor(ElementType.WATER));
        gc.fillRect(0, 0, getWidth(), getHeight()); //Fill the screen with WATER
        gc.setTransform(trans);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        if (model.getMapData() != null) {

            adjustWidthModifier();
            drawUserNodes();
            drawRelations();

            //Draw every elementType if option is enabled
            for (ElementType elementType : ElementType.values()) {
                try {
                    if (options.getBool(Option.valueOf("DISPLAY_"+elementType.toString()))) {
                        drawOrFill(elementType);
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("Failed to draw "+elementType);
                    System.out.println("There is no DISPLAY_"+elementType+" in the Option class!");
                }
            }

            //Display the kd-tree if option is enabled
            if (options.getBool(Option.USE_KD_TREE)) {
                if (options.getBool(Option.DISPLAY_KD_TREE)) {
                    depth = 0;

                    float maxX = model.getMapData().getMaxX();
                    float maxY = model.getMapData().getMaxY();
                    float minX = model.getMapData().getMinX();
                    float minY = model.getMapData().getMinY();

                    for (ElementType elementType : ElementType.values()) {
                        if (zoomLevel >= elementType.getZoomLevelRequired()) {
                            KdTree kdTree = model.getMapData().getKdTree(elementType);
                            drawKdTree(kdTree.getRoot(), maxX, maxY, minX, minY, 0.001);
                        }
                    }
                }
            }
            drawGraph();
        }
        gc.restore();

        //Calculate average repaint time
        time += System.nanoTime();
        totalLastRepaintTime += time;
        totalRepaints++;

        if (totalRepaints % 10 == 0) {
            lastTenAverageRepaintTime = totalLastRepaintTime / 10_000_000;
            System.out.println("Repaint time: " + time / 1_000_000 + " (Average last ten repaints: " + lastTenAverageRepaintTime + " total repaints: " + totalRepaints + ")");

            totalLastRepaintTime = 0;
        }
    }

    /**
     * Begins a range search for the specific tree if enabled.
     */
    public void rangeSearch() {
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

    /**
     * Draws every point of interest created by the user.
     */
    private void drawUserNodes() {
        gc.setStroke(Color.RED);
        gc.setLineWidth(0.002 * widthModifier);

        for (UserNode userNode : model.getMapData().getUserNodes()) {
            userNode.draw(gc, 0);
        }
    }

    /**
     * Draws a visualization of the directed graph if option is enabled.
     */
    private void drawGraph() {
        if (options.getBool(Option.DISPLAY_GRAPH)) {
            gc.setStroke(Color.DARKSLATEBLUE);
            gc.setLineWidth(0.0002 * widthModifier);

            for (Edge edge : model.getMapData().getDirectedGraph().getEdges()) {
                edge.draw(gc, zoomLevel);
            }
        }
    }

    /**
     * Draws a visualization of the kd-tree if option is enabled.
     */
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

    /**
     * Pans and repaints the MapCanvas with the given delta x and y.
     */
    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }

    /**
     * Zooms and repaints the MapCanvas with the given zoom factor.
     * Runs a range search task.
     */
    public void zoom(double zoomFactor, Point2D center) {
        double zoomLevelNext = zoomLevel * zoomFactor;

        if (zoomLevelNext < zoomLevelMax && zoomLevelNext > zoomLevelMin) {
            zoomLevel = zoomLevelNext;
            trans.prependScale(zoomFactor, zoomFactor, center);
            repaint();

            runRangeSearchTask();
        }
    }

    /**
     * Starts a new range search task.
     * Cancels the current range search task if it is running.
     * Repaints the MapCanvas when the task is finished.
     */
    public void runRangeSearchTask() {
        if (rangeSearchTask != null) {
            if (rangeSearchTask.isRunning()) {
                rangeSearchTask.cancel();
            }
        }
        rangeSearchTask = new Task<>() {
            @Override
            protected Void call() {
                rangeSearch();
                return null;
            }
        };
        rangeSearchTask.setOnSucceeded(e -> repaint());
        rangeSearchTask.setOnFailed(e -> rangeSearchTask.getException().printStackTrace());
        Thread thread = new Thread(rangeSearchTask);
        thread.start();
    }

    /**
     * Converts "screen" coordinates for a Point2D to "map" coordinates.
     * The new coordinates fit the values of OSM nodes.
     */
    public Point2D mouseToModelCoords(Point2D point) {
        try {
            return trans.inverseTransform(point);
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Draw every multipolygon Relation to display at the current zoom level.
     * Uses FillRule.EVEN_ODD to ensure that inner and
     * outer Ways of the Relation are properly filled.
     */
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

    /**
     * Draws or fills every Way with the ElementType to display at the current zoom level.
     * Retrieves values for colors, size and line dashes from the ElementType.
     */
    public void drawOrFill(ElementType elementType) {
        if (zoomLevel >= elementType.getZoomLevelRequired()) {
            gc.setLineDashes(elementType.getLineDashes());

            if (elementType.doFillDraw()) {
                gc.setFill(getColor(elementType));
                //for (Way way : model.getMapData().getFillWays(elementType, zoomLevel)) {
                for (Way way : model.getMapData().getWays(elementType)) {
                    way.fill(gc, zoomLevel);
                }
            } else {
                double size = elementType.getDrawSize() * widthModifier;

                gc.setStroke(getColor(elementType));
                gc.setLineWidth(size);
                for (Way line : model.getMapData().getWays(elementType)) {
                    line.draw(gc, zoomLevel);
                }
            }
        }
    }

    /**
     * @return Color for the specific ElementType depending on the current color mode.
     */
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

    /**
     * Adjust width modifier used to properly size Ways at the current zoom level
     */
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

    /**
     * Calculate zoom level percentage using the zoom limits.
     * @return zoom level percentage.
     */
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

    public double getZoomLevel() {
        return zoomLevel;
    }

    public String getZoomLevelText() {
        String value = "" + zoomLevel;
        if (value.length() > 8) {
            return value.substring(0, 8);
        }
        return value;
    }
}
