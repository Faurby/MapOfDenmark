package bfst21.osm;

import bfst21.view.Drawable;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.internal.Line2D;
import com.github.davidmoten.rtree2.internal.RectangleUtil;
import java.io.Serializable;


public class TreeWay extends Way implements Geometry, Drawable, Serializable {

    private static final long serialVersionUID = 3572193518160563298L;

    private float x1;
    private float y1;
    private float x2;
    private float y2;

    public TreeWay(long id) {
        super(id);
    }

    protected void updateBoundingBox(Node node) {
        if (nodes.size() == 1) {
            x1 = node.getX();
            x2 = node.getX();
            y1 = node.getY();
            y2 = node.getY();

        } else {
            float nX = node.getX();
            float nY = node.getY();

            if (nX < x1 && nY < y1) {
                x1 = nX;
                y1 = nY;
            }
            if (nX > x2 && nY > y2) {
                x2 = nX;
                y2 = nY;
            }
        }
    }

    @Override
    public void add(Node node) {
        super.add(node);
        updateBoundingBox(node);
    }

    @Override
    public double distance(Rectangle r) {
        if (r.contains(x1, y1) || r.contains(x2, y2)) {
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
        double d1 = line.ptSegDist(this.x1, this.y1);
        double d2 = line.ptSegDist(this.x2, this.y2);
        Line2D line2 = new Line2D(this.x1, this.y1, this.x2, this.y2);
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
        return Geometries.rectangle(x1, y1, x2, y2);
    }

    @Override
    public boolean intersects(Rectangle r) {
        return RectangleUtil.rectangleIntersectsLine(r.x1(), r.y1(), r.x2() - r.x1(),
                r.y2() - r.y1(), x1, y1, x2, y2);
    }

    @Override
    public boolean isDoublePrecision() {
        return false;
    }
}
