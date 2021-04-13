package bfst21.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bfst21.tree.BoundingBox;
import bfst21.view.Drawable;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Geometry;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.internal.Line2D;
import com.github.davidmoten.rtree2.internal.RectangleUtil;
import javafx.scene.canvas.GraphicsContext;


public class Way extends Element implements Geometry, Drawable, Serializable {

    private static final long serialVersionUID = 3139576893143362100L;
    protected List<Node> nodes = new ArrayList<>();

    private HashMap<String, String> tags;

    private float minX, maxX, minY, maxY;

    public Way(long id) {
        super(id);
    }

    protected void updateBoundingBox(Node node) {
        if (nodes.size() == 1) {
            minX = node.getX();
            maxX = node.getX();
            minY = node.getY();
            maxY = node.getY();

        } else {
            float nX = node.getX();
            float nY = node.getY();

            if (nX < minX) {
                minX = nX;
            }
            if (nY < minY) {
                minY = nY;
            }
            if (nX > maxX) {
                maxX = nX;
            }
            if (nY > maxY) {
                maxY = nY;
            }
        }
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(minX, maxX, minY, maxY);
    }

    private void createTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }
    }

    public void addTag(String key, String value) {
        createTags();
        tags.put(key, value);
    }

    public String getValue(String key) {
        createTags();
        return tags.get(key);
    }

    public HashMap<String, String> getTags() {
        createTags();
        return tags;
    }

    public Node first() {
        return nodes.get(0);
    }

    public Node last() {
        return nodes.get(nodes.size() - 1);
    }

    public void add(Node node) {
        nodes.add(node);
        updateBoundingBox(node);
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {
        gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());

        int inc = 1;
        if (zoomLevel <= 750) {
            inc = 10;
        } else if (zoomLevel <= 1050) {
            inc = 8;
        } else if (zoomLevel <= 1350) {
            inc = 6;
        } else if (zoomLevel <= 1800) {
            inc = 4;
        } else if (zoomLevel <= 2400) {
            inc = 2;
        }
        for (int i = 0; i < nodes.size(); i += inc) {
            if (i <= nodes.size() - 2) {
                Node node = nodes.get(i);
                gc.lineTo(node.getX(), node.getY());
            }
        }
        int last = nodes.size() - 1;
        gc.lineTo(nodes.get(last).getX(), nodes.get(last).getY());
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

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
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
