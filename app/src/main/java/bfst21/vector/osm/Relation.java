package bfst21.vector.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Relation extends Element implements Serializable {

    private static final long serialVersionUID = 4549832550595113105L;

    private final List<Node> nodes;
    private final List<Way> ways;
    private final List<Long> relationIDs;

    public Relation(long id) {
        super(id);
        nodes = new ArrayList<>();
        ways = new ArrayList<>();
        relationIDs = new ArrayList<>();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Way> getWays() {
        return ways;
    }

    public List<Long> getRelations() {
        return relationIDs;
    }

    public void addMember(Node node) {
        nodes.add(node);
    }

    public void addMember(Way way) {
        ways.add(way);
    }

    public void addMember(Long relationID) {
        relationIDs.add(relationID);
    }

    @Override
    public ElementType getType() {
        return ElementType.RELATION;
    }
}
