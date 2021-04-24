package bfst21.osm;

import bfst21.tree.BoundingBoxElement;
import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Relation extends BoundingBoxElement implements Serializable, Drawable {

    private static final long serialVersionUID = 4549832550595113105L;

    private final List<Node> nodes;
    private final List<Relation> relations;
    private List<Way> ways;

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
        List<Node> list = new ArrayList<>(nodes);
        for (Way way : ways) {
            list.addAll(way.getNodes());
        }
        return list;
    }

    public List<Way> getWays() {
        return ways;
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

    public void mergeOuterWays() {
        if (isMultipolygon()) {
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
                            if (way.first() == hasFirst.last()) {
                                //Some way is before this way
                                merged = Way.merge(hasFirst, way);

                            //Both ways have same node as their first
                            //So we need to reverse the way and add it AFTER hasFirst way
                            } else if (way.first() == hasFirst.first()) {
                                merged = Way.reverseMerge(hasFirst, way);
                            }
                        } else if (hasLast != null) {
                            if (way.last() == hasLast.first()) {
                                //Some way is after this way
                                merged = Way.merge(way, hasLast);

                            //Both ways have same node as their last
                            //So we need to reverse the way and add it AFTER hasLast way
                            } else if (way.last() == hasLast.last()) {
                                merged = Way.reverseMerge(hasLast, way);
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
                if (way.last() == node) {
                    mergedWayList.add(way);
                }
            });
            ways = mergedWayList;
        }
    }

    @Override
    public void trace(GraphicsContext gc, double zoomLevel) {

        for (Way way : getWays()) {
            String role = way.getRole();
            if (role != null) {
                List<Node> nodes = way.getNodes();

                if (role.equals("outer")) {
                    gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());

                    for (int i = 1; i < nodes.size(); i++) {
                        gc.lineTo(nodes.get(i).getX(), nodes.get(i).getY());
                    }
                } else if (role.equals("inner")) {
                    gc.moveTo(nodes.get(0).getX(), nodes.get(0).getY());

                    for (int i = 1; i < nodes.size(); i++) {
                        gc.lineTo(nodes.get(i).getX(), nodes.get(i).getY());
                    }
                }
            }
        }
    }
}
