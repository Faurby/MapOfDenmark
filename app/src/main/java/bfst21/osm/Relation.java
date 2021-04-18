package bfst21.osm;

import bfst21.tree.BoundingBoxElement;
import bfst21.view.Drawable;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.internal.Line2D;
import com.github.davidmoten.rtree2.internal.RectangleUtil;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Relation extends BoundingBoxElement implements Geometry, Serializable, Drawable {

    private static final long serialVersionUID = 4549832550595113105L;

    private final List<Node> nodes;
    private final List<Way> ways;
    private final List<Relation> relations;
    private ElementType elementType;
    private boolean multipolygon;
    private boolean initialBoundingBoxUpdate = true;

    public Relation(long id) {
        super(id);
        nodes = new ArrayList<>();
        ways = new ArrayList<>();
        relations = new ArrayList<>();
    }

    public boolean isMultipolygon() {
        return multipolygon;
    }

    public void setMultipolygon(boolean multipolygon) {
        this.multipolygon = multipolygon;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Way> getWays() {
        return ways;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void addMember(Node node) {
        nodes.add(node);

        updateBoundingBox(node, initialBoundingBoxUpdate);
        initialBoundingBoxUpdate = false;
    }

    public void addMember(Way way) {
        ways.add(way);

        for (Node node : way.getNodes()) {
            updateBoundingBox(node, initialBoundingBoxUpdate);
            initialBoundingBoxUpdate = false;
        }
    }

    public void addMember(Relation relation) {
        relations.add(relation);
    }

    public void setType(ElementType elementType) {
        this.elementType = elementType;
    }

    public ElementType getType() {
        return elementType;
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        for (Way way : getWays()) {
            String role = way.getRole();
            if (role != null) {
                if (role.equals("outer")) {
                    List<Node> nodes = way.getNodes();
                    gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());

                    for (int i = 1; i < nodes.size(); i++) {
                        gc.lineTo(nodes.get(i).getX(), nodes.get(i).getY());
                    }
                } else if (role.equals("inner")) {
                    List<Node> nodes = way.getNodes();
                    gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());

                    for (int i = 1; i < nodes.size(); i++) {
                        gc.lineTo(nodes.get(i).getX(), nodes.get(i).getY());
                    }
                }
            }
        }
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
