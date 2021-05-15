package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Relation is a group of elements.
 * It may contain coordinates and/or Ways.
 * <p>
 * Every Way in the Relation will be given the ElementType of
 * the Relation if it does not already have an ElementType.
 * <p>
 * Relations will only be drawn if it is a multipolygon Relation.
 * The rest can be drawn as individual Ways instead.
 * <p>
 * Extends BoundingBoxElement so it can be placed in a KD-tree.
 */
public class Relation extends BoundingBoxElement implements Serializable, Drawable {

    private static final long serialVersionUID = 4549832550595113105L;

    private List<Way> ways;

    private ElementType elementType;
    private boolean multipolygon;

    public Relation() {
        ways = new ArrayList<>();
    }

    /**
     * @return an array of coordinates for every Way in the relation.
     */
    public float[] getCoords() {

        int size = 0; //First we need to sum the amount of coordinates for every Way.
        for (Way way : ways) {
            size += way.getMapWay().getCoords().length;
        }
        float[] relationCoords = new float[size];

        int relationCoordsAmount = 0;
        for (Way way : ways) {
            float[] wayCoords = way.getMapWay().getCoords();

            for (int i = 0; i < wayCoords.length; i++) {
                relationCoords[i + relationCoordsAmount] = wayCoords[i];
            }
            relationCoordsAmount += wayCoords.length;
        }
        return relationCoords;
    }

    /**
     * Add a Way to the list of Ways in the Relation.
     * Update the bounding box values with every set of coordinates in the Way.
     */
    public void addWay(Way way) {
        ways.add(way);

        float[] coords = way.getMapWay().getCoords();
        for (int i = 0; i < coords.length; i += 2) {

            float x = coords[i];
            float y = coords[i + 1];

            updateBoundingBox(x, y);
        }
    }

    /**
     * Merge outer Ways of a Relation if it is a multipolygon.
     * Some Ways may have first/last coordinates in common so they need to be merged.
     * <p>
     * Some Ways have coordinates in the wrong order,
     * so we need to reverse the list of coordinates before correctly merging.
     */
    public void mergeOuterWays() {
        List<Way> mergedWayList = new ArrayList<>();

        Map<Node, Way> pieces = new HashMap<>();

        for (Way way : ways) {
            String role = way.getRole();
            if (role != null) {

                if (role.equals("outer")) {
                    Way hasFirst = pieces.remove(way.first());
                    Way hasLast = pieces.remove(way.last());

                    Way merged = null;

                    if (hasFirst != null) {
                        if (way.first().equals(hasFirst.last())) {
                            //Some way is before this way
                            merged = Way.merge(hasFirst, way, false);

                            //Both ways have same node as their first
                            //So we need to reverse the way and add it AFTER hasFirst way
                        } else if (way.first().equals(hasFirst.first())) {
                            merged = Way.merge(hasFirst, way, true);
                        }
                    } else if (hasLast != null) {
                        if (way.last().equals(hasLast.first())) {
                            //Some way is after this way
                            merged = Way.merge(way, hasLast, false);

                            //Both ways have same node as their last
                            //So we need to reverse the way and add it AFTER hasLast way
                        } else if (way.last().equals(hasLast.last())) {
                            merged = Way.merge(hasLast, way, true);
                        }
                    }
                    if (merged != null) {
                        merged.setRole("outer");
                        pieces.put(merged.first(), merged);
                        pieces.put(merged.last(), merged);

                    } else {
                        pieces.put(way.first(), way);
                        pieces.put(way.last(), way);
                    }
                } else {
                    mergedWayList.add(way);
                }
            } else {
                mergedWayList.add(way);
            }
        }
        pieces.forEach((node, way) -> {
            if (way.last().equals(node)) {
                mergedWayList.add(way);
            }
        });
        ways = mergedWayList;
    }

    /**
     * Draw a multipolygon Relation by tracing every outer/inner Way
     * then using FillRule.EVEN_ODD to correctly fill and draw the entire Relation.
     */
    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {

        for (Way way : ways) {
            String role = way.getRole();
            if (role != null) {
                float[] coords = way.getMapWay().getCoords();

                if (role.equals("outer") || role.equals("inner")) {
                    gc.moveTo(coords[0], coords[1]);

                    for (int i = 2; i < coords.length; i += 2) {
                        float x = coords[i];
                        float y = coords[i + 1];

                        gc.lineTo(x, y);
                    }
                }
            }
        }
    }

    public boolean isMultipolygon() {
        return multipolygon;
    }

    public void setMultipolygon(boolean multipolygon) {
        this.multipolygon = multipolygon;
    }

    public List<Way> getWays() {
        return ways;
    }

    public void setType(ElementType elementType) {
        this.elementType = elementType;
    }

    public ElementType getType() {
        return elementType;
    }
}
