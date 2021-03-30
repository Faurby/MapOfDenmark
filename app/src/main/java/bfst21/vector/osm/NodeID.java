package bfst21.vector.osm;


public class NodeID {

    private long id;
    private Node node;

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
