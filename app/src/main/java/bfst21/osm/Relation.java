package bfst21.osm;

import bfst21.view.Drawable;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Relation extends Element implements Serializable, Drawable {

    private static final long serialVersionUID = 4549832550595113105L;

    private final List<Node> nodes;
    private final List<Way> ways;
    private final List<Relation> relations;
    private ElementType elementType;
    private boolean multipolygon;

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
    }

    public void addMember(Way way) {
        ways.add(way);
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
}
