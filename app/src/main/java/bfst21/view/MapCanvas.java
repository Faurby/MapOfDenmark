package bfst21.view;

import bfst21.models.*;
import bfst21.osm.*;
import bfst21.pathfinding.*;
import bfst21.tree.BoundingBox;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;


public class MapCanvas extends Canvas {

    private Model model;

    private final double zoomLevelMin = 50.0D, zoomLevelMax = 200_000.0D;
    private double zoomLevel;
    private double widthModifier = 1.0D;

    private long totalRepaints, totalLastRepaintTime, lastTenAverageRepaintTime;

    private final DisplayOptions displayOptions = DisplayOptions.getInstance();
    private final GraphicsContext gc = getGraphicsContext2D();

    private Task<Void> rangeSearchTask;

    public float[] originCoords;
    public float[] destinationCoords;

    private ColorMode colorMode = ColorMode.STANDARD;
    private Affine trans = new Affine();

    private List<String> currentDirections = new ArrayList<>();
    private int currentRouteWeight;
    private float routeDistance;

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

        if (model.getMapData() != null) {
            pan(-model.getMapData().getMinX(), -model.getMapData().getMinY());

            zoomLevel = 1.0D;
            double zoomFactor = getWidth() / (model.getMapData().getMaxX() - model.getMapData().getMinX());

            zoom(zoomFactor, new Point2D(0, 0), true);
        }
    }

    /**
     * Repaints the MapCanvas.
     * Draws Ways for every ElementGroup to display at the current zoom level.
     * Draws every multipolygon relation.
     * Draws point of interests added by the user.
     * Draws dijkstra path if available.
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
            drawElementGroups();
            drawUserNodes();

            if (destinationCoords != null) {
                drawPathTo();
            }
            for (Pin pin : Pin.values()) {
                pin.draw(gc, zoomLevel);
            }
            drawMapText();
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

    /**
     * Draws every MapText found by the kd-tree range search.
     */
    private void drawMapText() {
        if (displayOptions.getBool(DisplayOption.DISPLAY_TEXT)) {
            List<MapText> list = model.getMapData().getMapTexts();

            if (list.size() > 0) {

                gc.setFill(getMapTextColor());
                gc.setTextAlign(TextAlignment.CENTER);

                for (MapText mapText : model.getMapData().getMapTexts()) {
                    mapText.draw(gc, zoomLevel);
                }
            }
        }
    }

    /**
     * Draw every ElementGroup if option is enabled
     */
    private void drawElementGroups() {
        for (ElementGroup elementGroup : ElementGroup.values()) {
            ElementType elementType = elementGroup.getType();
            try {
                if (elementType.isDisplayOptionEnabled()) {

                    //Workaround: Relations need to be drawn in the
                    //correct order together with other ElementTypes.
                    //So this is done to only draw Relations once per ElementType
                    ElementSize elementSize = elementGroup.getSize();

                    if (elementSize == ElementSize.DEFAULT
                     || elementSize == ElementSize.SMALL) {
                        drawRelations(elementType);
                    }
                    drawOrFill(elementGroup);
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Failed to draw " + elementGroup);
                System.out.println("There is no DISPLAY_" + elementType + " in the Option class!");
            }
        }
    }

    /**
     * Draws or fills every Way with the ElementGroup to display at the current zoom level.
     * Retrieves values for colors, size and line dashes from the ElementType.
     */
    private void drawOrFill(ElementGroup elementGroup) {
        if (elementGroup.doShowElement(zoomLevel)) {
            ElementType elementType = elementGroup.getType();
            gc.setLineDashes(elementType.getLineDashes());

            if (elementType.doFillDraw()) {
                gc.setFill(getColor(elementType));
                for (MapWay way : model.getMapData().getWays(elementGroup)) {
                    way.fill(gc, zoomLevel);
                }
            } else {
                double size = elementType.getDrawSize() * widthModifier;

                gc.setStroke(getColor(elementType));
                gc.setLineWidth(size);
                for (MapWay line : model.getMapData().getWays(elementGroup)) {
                    line.draw(gc, zoomLevel);
                }
            }
        }
    }

    /**
     * Draws every point of interest created by the user.
     */
    private void drawUserNodes() {
        for (UserNode userNode : model.getMapData().getUserNodes()) {
            userNode.draw(gc, zoomLevel, colorMode);
        }
    }

    /**
     * Determine if a dijkstra path currently exists.
     * A path exists if there are more than zero edges when calling pathTo().
     */
    public boolean doesDijkstraPathExist() {
        if (destinationCoords != null) {

            if (model.getMapData().getDijkstra() != null) {

                DirectedGraph directedGraph = model.getMapData().getDirectedGraph();
                int destinationID = directedGraph.getVertexID(destinationCoords);

                List<Edge> edgeList = model.getMapData().getDijkstra().pathTo(destinationID);

                return edgeList.size() > 0;
            }
        }
        return false;
    }

    /**
     * Draws a path from the origin coords to the destination coords.
     * <p>
     * Updates currentDirections to a list of readable directions.
     */
    private void drawPathTo() {
        DirectedGraph directedGraph = model.getMapData().getDirectedGraph();

        int destinationID = directedGraph.getVertexID(destinationCoords);

        if (model.getMapData().getDijkstra() != null) {
            DijkstraPath dijkstraPath = model.getMapData().getDijkstra();
            List<Edge> edgeList = dijkstraPath.pathTo(destinationID);

            if (edgeList.size() > 0) {

                gc.setStroke(Color.rgb(66, 133, 244));
                gc.setLineWidth(3.0D * (1.0D / Math.sqrt(trans.determinant())));

                currentDirections = new ArrayList<>();

                gc.beginPath();
                routeDistance = 0;
                float distanceSum = 0;
                int exitCount = 0;
                double weightSum = 0;

                if (edgeList.size() == 1) {
                    Edge before = edgeList.get(0);

                    before.draw(directedGraph, gc);
                    distanceSum = before.getDistance() * 1_000.0f;
                    routeDistance = distanceSum;
                    currentDirections.add("Follow " + before.getName() + " " + distanceSumToString(distanceSum));
                }

                for (int i = 0; i < (edgeList.size() - 1); i++) {
                    Edge before = edgeList.get(i);
                    Edge after = edgeList.get(i + 1);

                    weightSum += before.getWeight();

                    if (before.isJunction()) { //Is this a roundabout edge?
                        before.draw(directedGraph, gc);
                        int toID = before.getTo();
                        if (directedGraph.getOutDegree(toID) >= 2) {
                            exitCount++;
                        }
                        if (!after.isJunction()) {
                            after.draw(directedGraph, gc);
                            currentDirections.add("Take the " + exitCount + ". exit in the roundabout");
                            exitCount = 0;
                        }

                    } else {
                        Direction direction = directedGraph.getDirectionFromBearing(before, after);
                        float distanceBefore = before.getDistance() * 1_000f;
                        float distanceAfter = after.getDistance() * 1_000f;

                        distanceSum += distanceBefore;
                        routeDistance += distanceBefore;

                        before.draw(directedGraph, gc);

                        String dir = direction.toString().toLowerCase().replace("_", " ");
                        dir = dir.substring(0, 1).toUpperCase() + dir.substring(1);

                        //If direction isn't Direction.STRAIGHT, we know
                        //that a turn is coming up unless its a roundabout.
                        if (direction != Direction.STRAIGHT) {
                            currentDirections.add("Follow " + before.getName() + " " + distanceSumToString(distanceSum));
                            if (!after.isJunction()) {
                                currentDirections.add(dir + " down " + after.getName());
                            }
                            distanceSum = 0;

                            //If direction is Direction.STRAIGHT but we find a new Way name.
                        } else if (!before.getName().equals(after.getName())) {
                            currentDirections.add("Follow " + before.getName() + " " + distanceSumToString(distanceSum));
                            distanceSum = 0;
                        }
                        if (i == (edgeList.size() - 2)) { //Last iteration in the list
                            after.draw(directedGraph, gc);

                            weightSum += after.getWeight();
                            currentRouteWeight = (int) Math.ceil(weightSum);
                            distanceSum += distanceAfter;
                            routeDistance += distanceAfter;
                            currentDirections.add("Follow " + after.getName() + " " + distanceSumToString(distanceSum));
                        }
                    }
                }
                currentDirections.add("Arrive at your destination");
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

    /**
     * Change the current view of the screen to the given x and y values.
     */
    public void changeView(float newX, float newY) {

        double x1 = trans.getTx() / Math.sqrt(trans.determinant());
        double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
        double x2 = getWidth() - x1;
        double y2 = getHeight() - y1;

        Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
        Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

        double oldX = (p1.getX() + p2.getX()) / 2.0D;
        double oldY = (p1.getY() + p2.getY()) / 2.0D;

        double dx = (newX - oldX) * zoomLevel;
        double dy = (newY - oldY) * zoomLevel;

        pan(-dx, -dy);
        rangeSearch();
        repaint();
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

    /**
     * Run dijkstra path finding for origin and destination coordinates.
     * Set PINs visible for origin and destination.
     */
    public void runDijkstra() {
        model.getMapData().runDijkstra(originCoords, destinationCoords);

        Pin.ORIGIN.setCoords(originCoords);
        Pin.ORIGIN.setVisible(true);

        Pin.DESTINATION.setCoords(destinationCoords);
        Pin.DESTINATION.setVisible(true);
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
    private void rangeSearch() {
        if (model.getMapData() != null) {
            model.getMapData().kdTreeRangeSearch(getScreenBoundingBox(true), zoomLevel);
        }
    }

    /**
     * @return BoundingBox of the screen.
     */
    public BoundingBox getScreenBoundingBox(boolean extend) {
        double x1 = trans.getTx() / Math.sqrt(trans.determinant());
        double y1 = (-trans.getTy()) / Math.sqrt(trans.determinant());
        double x2 = getWidth() - x1;
        double y2 = getHeight() - y1;

        if (extend) {
            x1 -= 50.0D;
            y1 -= 50.0D;
            x2 += 50.0D;
            y2 += 50.0D;
        }

        Point2D p1 = mouseToModelCoords(new Point2D(x1, y1));
        Point2D p2 = mouseToModelCoords(new Point2D(x2, y2));

        return new BoundingBox((float) p1.getX(), (float) p2.getX(), (float) p1.getY(), (float) p2.getY());
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
    private Color getColor(ElementType elementType) {
        if (colorMode == ColorMode.COLOR_BLIND) {
            return elementType.getColorBlind();
        } else if (colorMode == ColorMode.DARK_MODE) {
            return elementType.getBlackWhite();
        }
        return elementType.getColor();
    }

    /**
     * @return Color for MapText depending on the current color mode.
     */
    private Color getMapTextColor() {
        if (colorMode == ColorMode.COLOR_BLIND) {
            return Color.rgb(220, 255, 255);
        } else if (colorMode == ColorMode.DARK_MODE) {
            return Color.rgb(180, 180, 180);
        }
        return Color.rgb(12, 12, 15);
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    /**
     * Adjust width modifier used to properly size Ways at the current zoom level
     */
    private void adjustWidthModifier() {
        if (zoomLevel < 500D) {
            widthModifier = 1.0D;

        } else if (zoomLevel < 2_000.0D) {
            widthModifier = 0.75D;

        } else if (zoomLevel < 6_000.0D) {
            widthModifier = 0.50D;

        } else if (zoomLevel < 22_000.0D) {
            widthModifier = 0.25D;
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
        if (zoomLevel == 0.0D) {
            return 100 + "%";
        } else {
            double max = Math.log(zoomLevelMax);
            double min = Math.log(zoomLevelMin);
            double diff = max - min;
            double forOnePercent = diff / 100.0D;

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

    /**
     * Reset current route values.
     */
    public void resetCurrentRoute() {
        currentDirections = new ArrayList<>();
        currentRouteWeight = 0;
    }

    /**
     * Format route duration to a readable String.
     *
     * @return route duration as a formatted String.
     */
    public String getCurrentRouteDuration() {
        int duration = currentRouteWeight;

        TransportOption current = TransportOptions.getInstance().getCurrentlyEnabled();
        if (current == TransportOption.BIKE) {
            duration = (int) Math.ceil(routeDistance / 1000.0f * 60.0f / 15.0f);
        } else if (current == TransportOption.WALK) {
            duration = (int) Math.ceil(routeDistance / 1000.0f * 60.0f / 5.0f);
        }

        if (duration > 60) {
            int hours = duration / 60;
            int minutes = duration % 60;
            String minutesString = "";
            if (minutes != 0) {
                minutesString = minutes + " minute(s)";
            }
            return "Estimated duration: " + hours + " hour(s) " + minutesString;
        } else {
            return "Estimated duration: " + duration + " min";
        }
    }

    /**
     * Format distance sum to a readable String.
     *
     * @return distance as a formatted String.
     */
    private String distanceSumToString(float distance) {
        if (distance >= 1_000.0f) {
            distance /= 1_000.0f;
            String distanceString = "" + distance;

            if (distanceString.contains(".")) {
                String[] split = distanceString.split("\\.");
                return split[0] + "." + split[1].charAt(0) + " km";

            } else {
                return distanceString + " km";
            }

        } else if (distance > 10.0f) {
            distance = (Math.round(distance / 10.0f) * 10.0f);
            return (int) distance + " m";

        }
        return (int) distance + " m";
    }

    public String getRouteDistanceToString() {
        return "Distance: " + distanceSumToString(routeDistance);
    }

    public Model getModel() {
        return model;
    }

    public Affine getTrans() {
        return trans;
    }

    public List<String> getCurrentDirections() {
        return currentDirections;
    }
}
