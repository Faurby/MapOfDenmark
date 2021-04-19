package bfst21.osm;


public class NodeID extends Element {

    private static final long serialVersionUID = -5720053995520236497L;
    private final Node node;

    public NodeID(long id, Node node) {
        super(id);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
