package bfst21.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import bfst21.tree.BoundingBoxElement;
import bfst21.view.Drawable;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.internal.Line2D;
import com.github.davidmoten.rtree2.internal.RectangleUtil;
import javafx.scene.canvas.GraphicsContext;


public class Way extends BoundingBoxElement implements Geometry, Drawable, Serializable {

    private static final long serialVersionUID = 3139576893143362100L;
    private final List<Node> nodes = new ArrayList<>();

    private ElementType elementType;
    private String role;
    private int maxSpeed;
    private boolean isDrawn;

    public Way(long id) {
        super(id);
    }

    public ElementSize getElementSize() {
        if (elementType.hasMultipleSizes()) {
            double xLength = maxX - minX;
            double yLength = maxY - minY;
            double areaSize = (xLength * yLength * Math.pow(10.0D, 9.0D));
            return ElementSize.getSize(areaSize);
        }
        return ElementSize.DEFAULT;
    }

    public void add(Node node) {
        nodes.add(node);

        boolean initialNode = nodes.size() == 1;
        updateBoundingBox(node, initialNode);
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());

        int nodeSkipAmount = getNodeSkipAmount(zoomLevel);
        for (int i = 1; i < nodes.size(); i += nodeSkipAmount) {
            if (i <= nodes.size() - 2) {
                Node node = nodes.get(i);
                gc.lineTo(node.getX(), node.getY());
            }
        }
        int last = nodes.size() - 1;
        gc.lineTo(nodes.get(last).getX(), nodes.get(last).getY());
        isDrawn = true;
    }

    public static int getNodeSkipAmount(double zoomLevel) {
        if (zoomLevel <= 100) {
            return 10;
        } else if (zoomLevel <= 140) {
            return 9;
        } else if (zoomLevel <= 190) {
            return 8;
        } else if (zoomLevel <= 270) {
            return 7;
        } else if (zoomLevel <= 350) {
            return 6;
        } else if (zoomLevel <= 500) {
            return 5;
        } else if (zoomLevel <= 700) {
            return 4;
        } else if (zoomLevel <= 950) {
            return 3;
        } else if (zoomLevel <= 1350) {
            return 2;
        }
        return 1;
    }

    public static Way merge(Way first, Way second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        Way merged = new Way(first.getID());
        merged.nodes.addAll(first.nodes);
        merged.nodes.addAll(second.nodes.subList(1, second.nodes.size()));
        return merged;
    }

    public static Way merge(Way before, Way coast, Way after) {
        return merge(merge(before, coast), after);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Way other = (Way) obj;
        if (nodes == null) {
            return other.nodes == null;
        } else {
            return nodes.equals(other.nodes);
        }
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public ElementType getType() {
        return elementType;
    }

    public void setType(ElementType elementType) {
        this.elementType = elementType;
    }

    public boolean canNavigate() {
        return elementType == ElementType.PRIMARY ||
                elementType == ElementType.MOTORWAY ||
                elementType == ElementType.TRUNK ||
                elementType == ElementType.TERTIARY ||
                elementType == ElementType.CYCLEWAY ||
                elementType == ElementType.RESIDENTIAL ||
                elementType == ElementType.ROAD ||
                elementType == ElementType.FOOTWAY;
    }

    public boolean canDrive() {
        return elementType == ElementType.PRIMARY ||
                elementType == ElementType.MOTORWAY ||
                elementType == ElementType.RESIDENTIAL ||
                elementType == ElementType.TERTIARY ||
                elementType == ElementType.TRUNK;
    }

    public boolean canBike() {
        return elementType == ElementType.TERTIARY ||
                elementType == ElementType.CYCLEWAY ||
                elementType == ElementType.ROAD ||
                elementType == ElementType.RESIDENTIAL;
    }

    public boolean canWalk() {
        return elementType == ElementType.TERTIARY ||
                elementType == ElementType.CYCLEWAY ||
                elementType == ElementType.RESIDENTIAL ||
                elementType == ElementType.ROAD ||
                elementType == ElementType.FOOTWAY;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node first() {
        return nodes.get(0);
    }

    public Node last() {
        return nodes.get(nodes.size() - 1);
    }

    public boolean isDrawn() {
        return isDrawn;
    }

    @Override
    public double distance(Rectangle r) {
        if (r.contains(minX, minY) || r.contains(maxX, maxY)) {
            return 0;
        } else {
            double d1 = distance(r.x1(), r.y1(), r.x1(), r.y2());
            if (d1 == 0) {
                return 0;
            }
            double d2 = distance(r.x1(), r.y2(), r.x2(), r.y2());
            if (d2 == 0) {
                return 0;
            }
            double d3 = distance(r.x2(), r.y2(), r.x2(), r.y1());
            double d4 = distance(r.x2(), r.y1(), r.x1(), r.y1());
            return Math.min(d1, Math.min(d2, Math.min(d3, d4)));
        }
    }

    private double distance(double x1, double y1, double x2, double y2) {
        Line2D line = new Line2D(x1, y1, x2, y2);
        double d1 = line.ptSegDist(this.minX, this.minY);
        double d2 = line.ptSegDist(this.maxX, this.maxY);
        Line2D line2 = new Line2D(this.minX, this.minY, this.maxX, this.maxY);
        double d3 = line2.ptSegDist(x1, y1);
        if (d3 == 0) {
            return 0;
        }
        double d4 = line2.ptSegDist(x2, y2);
        if (d4 == 0) {
            return 0;
        } else {
            return Math.min(d1, Math.min(d2, Math.min(d3, d4)));
        }
    }

    @Override
    public Rectangle mbr() {
        return Geometries.rectangle(minX, minY, maxX, maxY);
    }

    @Override
    public boolean intersects(Rectangle r) {
        return RectangleUtil.rectangleIntersectsLine(r.x1(), r.y1(), r.x2() - r.x1(),
                r.y2() - r.y1(), minX, minY, maxX, maxY);
    }

    @Override
    public boolean isDoublePrecision() {
        return false;
    }
}
