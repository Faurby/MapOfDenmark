package bfst21.view;

import bfst21.models.DisplayOptions;
import bfst21.models.DisplayOption;
import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.pathfinding.Edge;
import bfst21.pathfinding.Vertex;
import bfst21.tree.BoundingBox;
import bfst21.tree.KdNode;
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
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class MapCanvas extends Canvas {

    private Model model;

    private final double zoomLevelMin = 50.0D, zoomLevelMax = 100_000.0D;
    private double zoomLevel;
    private double widthModifier = 1.0D;

    private long totalRepaints, totalLastRepaintTime, lastTenAverageRepaintTime;

    private final DisplayOptions displayOptions = DisplayOptions.getInstance();
    private final GraphicsContext gc = getGraphicsContext2D();

    private int depth;
    private Task<Void> rangeSearchTask;
    private Task<Void> nearestNeighborTask;

    private ColorMode colorMode = ColorMode.STANDARD;
    private Affine trans = new Affine();

    private float[] nearestNeighborCoords;
    private float[] searchAddressCoords;

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
     *
     * @param loadDefaultFile determines if the default or selected file should be loaded.
     */
    public void load(boolean loadDefaultFile) throws XMLStreamException, IOException, ClassNotFoundException {
        model.load(loadDefaultFile);
        trans = new Affine();

        pan(-model.getMapData().getMinX(), -model.getMapData().getMinY());

        zoomLevel = 1.0D;
        double zoomFactor = getWidth() / (model.getMapData().getMaxX() - model.getMapData().getMinX());

        zoom(zoomFactor, new Point2D(0, 0), true);
    }

    /**
     * Repaints the MapCanvas.
     * Draws Ways for every ElementGroup to display at the current zoom level.
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

            //Draw every elementGroup if option is enabled
            for (ElementGroup elementGroup : ElementGroup.values()) {
                ElementType elementType = elementGroup.getType();
                try {
                    if (elementType.isDisplayOptionEnabled()) {

                        //TODO: This is a bit scuffed
                        // but relations needs to be drawn in the correct order somehow...
                        ElementSize elementSize = elementGroup.getSize();
                        if (elementSize == ElementSize.DEFAULT || elementSize == ElementSize.SMALL) {
                            drawRelations(elementType);
                        }
                        drawOrFill(elementGroup);
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("Failed to draw " + elementGroup);
                    System.out.println("There is no DISPLAY_" + elementType + " in the Option class!");
                }
            }
            drawUserNodes();
            drawNeighborNodes();
            //drawSearchAddress();
            drawMapText();

            //Display the kd-tree if option is enabled
            if (displayOptions.getBool(DisplayOption.DISPLAY_KD_TREE)) {
                depth = 0;

                float maxX = model.getMapData().getMaxX();
                float maxY = model.getMapData().getMaxY();
                float minX = model.getMapData().getMinX();
                float minY = model.getMapData().getMinY();

                for (ElementGroup elementGroup : ElementGroup.values()) {
                    if (elementGroup.doShowElement(zoomLevel)) {

                        if (elementGroup.getType().isDisplayOptionEnabled()) {

                            KdTree<Way> kdTree = model.getMapData().getKdTree(elementGroup);
                            if (kdTree != null) {
                                drawKdTree(kdTree.getRoot(), maxX, maxY, minX, minY, 0.001);
                            }
                        }
                    }
                }
            }
            drawGraph();
            if (model.getMapData().destinationCoords != null) {
                drawPathTo(model.getMapData().destinationCoords);
            }
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
     * Draw every multipolygon Relation to display at the current zoom level.
     * Uses FillRule.EVEN_ODD to ensure that inner and
     * outer Ways of the Relation are properly filled.
     */
    public void drawRelations(ElementType elementType) {
        if (displayOptions.getBool(DisplayOption.DISPLAY_RELATIONS)) {
            if (elementType.doShowElement(zoomLevel)) {

                gc.setFillRule(FillRule.EVEN_ODD);

                for (Relation rel : model.getMapData().getRelations(elementType)) {
                    gc.setFill(getColor(elementType));
                    rel.fill(gc, zoomLevel);
                }
                gc.setFillRule(FillRule.NON_ZERO);
            }
        }
    }

    public void drawMapText() {
        if (displayOptions.getBool(DisplayOption.DISPLAY_TEXT)) {
            List<MapText> list = model.getMapData().getMapTexts();

            if (list.size() > 0) {

                gc.setFill(getTextColor());
                gc.setTextAlign(TextAlignment.CENTER);
                String font = "Calibri";

                for (MapText mapText : model.getMapData().getMapTexts()) {

                    if (zoomLevel < 1000 && mapText.canDrawFarAway(mapText.getMapTextType())) {
                        gc.setFont(new Font(font, 0.08 * widthModifier));
                        gc.fillText(mapText.getName(), mapText.getCoords()[0], mapText.getCoords()[1]);

                    } else if (zoomLevel < 40_000 && mapText.canDraw(zoomLevel)) {
                        gc.setFont(new Font(font, mapText.getMapTextType().getStandardMultiplier() * widthModifier));
                        gc.fillText(mapText.getName(), mapText.getCoords()[0], mapText.getCoords()[1]);

                    } else if (zoomLevel >= 40_000) {
                        gc.setFont(new Font(font, 0.002 * widthModifier));
                        gc.fillText(mapText.getName(), mapText.getCoords()[0], mapText.getCoords()[1]);
                    }
                }
            }
        }
    }

    /**
     * Draws or fills every Way with the ElementGroup to display at the current zoom level.
     * Retrieves values for colors, size and line dashes from the ElementType.
     */
    public void drawOrFill(ElementGroup elementGroup) {
        if (elementGroup.doShowElement(zoomLevel)) {
            ElementType elementType = elementGroup.getType();
            gc.setLineDashes(elementType.getLineDashes());

            if (elementType.doFillDraw()) {
                gc.setFill(getColor(elementType));
                for (Way way : model.getMapData().getWays(elementGroup)) {
                    way.fill(gc, zoomLevel);
                }
            } else {
                double size = elementType.getDrawSize() * widthModifier;

                gc.setStroke(getColor(elementType));
                gc.setLineWidth(size);
                for (Way line : model.getMapData().getWays(elementGroup)) {
                    line.draw(gc, zoomLevel);
                }
            }
        }
    }

    /**
     * Draws every point of interest created by the user.
     */
    private void drawUserNodes() {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(10 * (1 / Math.sqrt(trans.determinant())));

        for (UserNode userNode : model.getMapData().getUserNodes()) {
            userNode.draw(gc, 0);
        }
    }

    /**
     * Draw the entire navigation graph.
     */
    public void drawGraph() {
        if (displayOptions.getBool(DisplayOption.DISPLAY_GRAPH)) {
            DirectedGraph directedGraph = model.getMapData().getDirectedGraph();

            gc.setStroke(Color.DARKSLATEBLUE);
            gc.setLineWidth(0.0002 * widthModifier);
            gc.beginPath();

            Vertex[] vertices = directedGraph.getVertices();

            for (Vertex vertex : vertices) {
                if (vertex != null) {
                    for (int id : vertex.getEdges()) {
                        Edge edge = directedGraph.getEdge(id);
                        edge.draw(directedGraph, gc);
                    }
                }
            }
            gc.stroke();
        }
    }

    /**
     * Draws a path from the origin coords to the destination coords.
     * Draws every path that dijkstra has investigated.
     */
    public void drawPathTo(float[] destinationCoords) {
        if (displayOptions.getBool(DisplayOption.DISPLAY_DIJKSTRA)) {
            DirectedGraph directedGraph = model.getMapData().getDirectedGraph();

            gc.setStroke(Color.DARKSLATEBLUE);
            gc.setLineWidth(0.0002 * widthModifier);

            gc.beginPath();
            Edge[] edges = model.getMapData().getDijkstra().getEdgeTo();
            for (Edge edge : edges) {
                if (edge != null) {
                    edge.draw(directedGraph, gc);
                }
            }
            gc.stroke();

            gc.setStroke(Color.RED);
            gc.setLineWidth(0.0004 * widthModifier);

            int destinationID = directedGraph.getVertexID(destinationCoords);

            Iterable<Edge> it = model.getMapData().getDijkstra().pathTo(destinationID);
            if (it != null) {
                gc.beginPath();

                for (Edge edge : it) {
                    edge.draw(directedGraph, gc);
                }
                gc.stroke();
            }
        }
    }

    /**
     * Pans and repaints the MapCanvas with the given delta x and y.
     */
    public void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }

    public void changeView(float newX, float newY){
        searchAddressCoords = new float[]{newX, newY};

        double x1 = trans.getTx() / Math.sqrt(trans.determinant());
        double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
        double x2 = getWidth() - x1;
        double y2 = getHeight() - y1;

        Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
        Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

        double oldX = (p1.getX() + p2.getX()) / 2;
        double oldY = (p1.getY() + p2.getY()) / 2;

        double dx = newX - oldX;
        double dy = newY - oldY;

        dx *= zoomLevel;
        dy *= zoomLevel;
        pan(-dx, -dy);
        rangeSearch();
        repaint();
    }

    public void drawSearchAddress() {
        if (searchAddressCoords != null) {
            double x1 = trans.getTx() / Math.sqrt(trans.determinant());
            double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
            double x2 = getWidth() - x1;
            double y2 = getHeight() - y1;

            Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
            Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

            double oldX = (p1.getX() + p2.getX()) / 2;
            double oldY = (p1.getY() + p2.getY()) / 2;

            gc.setStroke(Color.GREEN);
            gc.setLineWidth(0.004 * widthModifier);

            gc.beginPath();
            gc.moveTo(searchAddressCoords[0], searchAddressCoords[1]);
            gc.lineTo(searchAddressCoords[0], searchAddressCoords[1]);
            gc.moveTo(oldX, oldY);
            gc.lineTo(oldX, oldY);
            gc.stroke();
        }
    }

    /**
     * Zooms and repaints the MapCanvas with the given zoom factor.
     * Runs a range search task unless it is the initial zoom after loading the map.
     */
    public void zoom(double zoomFactor, Point2D center, boolean initialZoom) {
        double zoomLevelNext = zoomLevel * zoomFactor;

        if (zoomLevelNext < zoomLevelMax && zoomLevelNext > zoomLevelMin) {
            zoomLevel = zoomLevelNext;
            trans.prependScale(zoomFactor, zoomFactor, center);
            repaint();

            if (!initialZoom) {
                runRangeSearchTask();
            }
        }
    }

    public void drawNeighborNodes() {
        if (nearestNeighborCoords != null) {

            gc.setStroke(Color.RED);
            gc.setLineWidth(0.002 * widthModifier);

            gc.beginPath();
            gc.moveTo(nearestNeighborCoords[0], nearestNeighborCoords[1]);
            gc.lineTo(nearestNeighborCoords[0], nearestNeighborCoords[1]);
            gc.stroke();
        }
    }

    /**
     * Starts a new nearest neighbor search task.
     * Cancels the current nearest neighbor search task if it is running.
     * Repaints the MapCanvas when the task is finished.
     */
    public void runNearestNeighborTask(float[] queryCoords) {
        if (nearestNeighborTask != null) {
            if (nearestNeighborTask.isRunning()) {
                nearestNeighborTask.cancel();
            }
        }
        nearestNeighborTask = new Task<>() {
            @Override
            protected Void call() {
                nearestNeighborCoords = model.getMapData().kdTreeNearestNeighborSearch(queryCoords);
                return null;
            }
        };
        nearestNeighborTask.setOnSucceeded(e -> repaint());
        nearestNeighborTask.setOnFailed(e -> nearestNeighborTask.getException().printStackTrace());
        Thread thread = new Thread(nearestNeighborTask);
        thread.start();
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
     * Begins a range search for the kd-tree if MapData is available.
     */
    public void rangeSearch() {
        if (model.getMapData() != null) {
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

            lineWidth *= 0.7D;

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
     * @return Color for the specific ElementType depending on the current color mode.
     */
    public Color getColor(ElementType elementType) {
        if (colorMode == ColorMode.COLOR_BLIND) {
            return elementType.getColorBlind();
        } else if (colorMode == ColorMode.DARK_MODE) {
            return elementType.getBlackWhite();
        }
        return elementType.getColor();
    }

    public Color getTextColor() {
        if (colorMode == ColorMode.COLOR_BLIND) {
            return Color.rgb(220, 255, 255);
        } else if (colorMode == ColorMode.DARK_MODE) {
            return Color.rgb(180, 180, 180);
        }
        return Color.rgb(4, 1, 10);
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
     *
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

    public Model getModel() {
        return model;
    }

    public Affine getTrans() {
        return trans;
    }

}
