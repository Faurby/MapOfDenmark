package bfst21.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Relation extends Element implements Serializable {

    private static final long serialVersionUID = 4549832550595113105L;

    private final List<Node> nodes;
    private final List<Way> ways;
    private final List<Relation> relations;

    public Relation(long id) {
        super(id);
        nodes = new ArrayList<>();
        ways = new ArrayList<>();
        relations = new ArrayList<>();
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
}
