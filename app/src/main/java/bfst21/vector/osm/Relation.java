package bfst21.vector.osm;

import java.util.List;


public class Relation {

    private List<Node> nodes;
    private List<Way> ways;
    private List<Relation> relations;

    public Relation(List<Node> nodes, List<Way> ways, List<Relation> relations) {
        this.nodes = nodes;
        this.ways = ways;
        this.relations = relations;
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
}
