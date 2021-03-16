package bfst21.vector.osm;

import java.util.ArrayList;
import java.util.List;


public class Relation extends Element {

    private List<Node> nodes;
    private List<Way> ways;
    private List<Long> relationIDs;

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
