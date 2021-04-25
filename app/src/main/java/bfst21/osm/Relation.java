package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Relation extends BoundingBoxElement implements Serializable, Drawable {

    private static final long serialVersionUID = 4549832550595113105L;

    private final List<Relation> relations;
    private List<Way> ways;

    private ElementType elementType;
    private boolean multipolygon;
    private boolean initialBoundingBoxUpdate = true;

    public Relation(long id) {
        super(id);
        ways = new ArrayList<>();
        relations = new ArrayList<>();
    }

    public float[] getCoords() {
        float[] relationCoords = new float[2];

        int relationCoordsAmount = 0;
        for (Way way : ways) {

            float[] wayCoords = way.getCoords();
            int wayCoordsSize = wayCoords.length;
            int relationCoordsSize = relationCoords.length;
            int newAmount = relationCoordsAmount + wayCoordsSize;

            if (newAmount >= relationCoordsSize) {
                float[] copy = new float[newAmount * 2];
                for (int i = 0; i < coordsAmount; i++) {
                    copy[i] = relationCoords[i];
                }
                relationCoords = copy;
            }
            for (int i = 0; i < wayCoordsSize; i++) {
                relationCoords[i + relationCoordsAmount] = wayCoords[i];
            }
            relationCoordsAmount = newAmount;
        }
        return relationCoords;
    }

    public void addWay(Way way) {
        ways.add(way);

        float[] coords = way.getCoords();
        for (int i = 0; i < coords.length; i += 2) {

            float x = coords[i];
            float y = coords[i + 1];

            updateBoundingBox(new float[]{x, y}, initialBoundingBoxUpdate);
            initialBoundingBoxUpdate = false;
        }
    }

    public void mergeOuterWays() {
        if (multipolygon) {
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
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {

        for (Way way : ways) {
            String role = way.getRole();
            if (role != null) {
                float[] coords = way.getCoords();

                if (role.equals("outer") || role.equals("inner")) {
                    gc.moveTo(coords[0], coords[1]);

                    for (int i = 2; i < way.getCoordsAmount(); i += 2) {
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

    public void addRelation(Relation relation) {
        relations.add(relation);
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setType(ElementType elementType) {
        this.elementType = elementType;
    }

    public ElementType getType() {
        return elementType;
    }
}
