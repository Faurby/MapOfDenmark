package bfst21.osm;


public class NodeID {

    private final long id;
    private final Node node;

    public NodeID(long id, Node node) {
        this.id = id;
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public long getID() {
        return id;
    }
}
